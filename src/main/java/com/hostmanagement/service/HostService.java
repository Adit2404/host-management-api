package com.hostmanagement.service;

import com.hostmanagement.dto.HostGroup;
import com.hostmanagement.model.Host;
import com.hostmanagement.repo.HostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for host management operations.
 * Handles business logic for host CRUD operations, grouping, and merging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HostService {

    private final HostRepository hostRepository;
    private final HostSimilarityService similarityService;

    /**
     * Retrieves all host groups (both consolidated and non-consolidated)
     */
    public List<HostGroup> getAllHostGroups() {
        List<Host> allHosts = hostRepository.findAll();
        List<Host> nonConsolidated = allHosts.stream()
                .filter(h -> !h.isConsolidated())
                .collect(Collectors.toList());

        List<Host> consolidated = allHosts.stream()
                .filter(Host::isConsolidated)
                .collect(Collectors.toList());

        List<HostGroup> groups = new ArrayList<>();

        // Group non-consolidated hosts by similarity
        Map<Host, List<Host>> similarGroups = similarityService.groupSimilarHosts(nonConsolidated);

        for (Map.Entry<Host, List<Host>> entry : similarGroups.entrySet()) {
            List<Host> groupHosts = entry.getValue();
            HostGroup group = HostGroup.builder()
                    .groupId(entry.getKey().getId())
                    .primaryHost(groupHosts.get(0))
                    .similarHosts(groupHosts)
                    .totalRecords(groupHosts.size())
                    .consolidated(false)
                    .build();
            groups.add(group);
        }

        // Add consolidated hosts as single-item groups
        for (Host host : consolidated) {
            HostGroup group = HostGroup.builder()
                    .groupId(host.getId())
                    .primaryHost(host)
                    .similarHosts(Collections.singletonList(host))
                    .totalRecords(1)
                    .consolidated(true)
                    .build();
            groups.add(group);
        }

        // Sort: non-consolidated first, then by record count, then by IP
        groups.sort((g1, g2) -> {
            if (g1.isConsolidated() != g2.isConsolidated()) {
                return g1.isConsolidated() ? 1 : -1;
            }
            int recordCompare = Integer.compare(g2.getTotalRecords(), g1.getTotalRecords());
            if (recordCompare != 0) {
                return recordCompare;
            }
            String ip1 = g1.getPrimaryHost().getIpAddress();
            String ip2 = g2.getPrimaryHost().getIpAddress();
            if (ip1 != null && ip2 != null) {
                return ip1.compareTo(ip2);
            }
            return 0;
        });

        return groups;
    }

    /**
     * Retrieves a specific host group by group ID
     */
    public Optional<HostGroup> getHostGroup(String groupId) {
        return getAllHostGroups().stream()
                .filter(g -> g.getGroupId().equals(groupId))
                .findFirst();
    }

    /**
     * Retrieves a single host by ID
     */
    public Optional<Host> getHostById(String id) {
        return hostRepository.findById(id);
    }

    /**
     * Adds a new host record
     */
    public Host addHost(Host host) {
        if (!host.isValid()) {
            throw new IllegalArgumentException("Host must have at least one non-empty property");
        }

        if (host.getId() == null) {
            host.setId(UUID.randomUUID().toString());
        }

        if (host.getCreatedAt() == null) {
            host.setCreatedAt(java.time.LocalDateTime.now());
        }

        return hostRepository.save(host);
    }

    /**
     * Merges multiple host records using a simple "keep first, add unique" strategy
     *
     * Simple Merge Logic:
     * - IP Address: Keep from the first host
     * - Names: Add all unique names from all hosts
     * - Roles: Add all unique roles from all hosts
     * - Protocols: Add all unique protocols from all hosts
     * - OS: Keep from the first host (or first non-null)
     */
    public Host mergeHosts(List<String> hostIds) {
        if (hostIds == null || hostIds.isEmpty()) {
            throw new IllegalArgumentException("At least one host ID must be provided");
        }

        List<Host> hostsToMerge = hostIds.stream()
                .map(hostRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (hostsToMerge.isEmpty()) {
            throw new IllegalArgumentException("No valid hosts found with provided IDs");
        }

        if (hostsToMerge.size() != hostIds.size()) {
            log.warn("Some host IDs were not found. Requested: {}, Found: {}",
                    hostIds.size(), hostsToMerge.size());
        }

        // Create consolidated host with simple merge strategy
        Host consolidated = mergeHostsSimple(hostsToMerge);
        consolidated.setConsolidated(true);
        consolidated.setSourceRecordIds(new HashSet<>(hostIds));

        // Save consolidated host
        Host saved = hostRepository.save(consolidated);

        // Delete original hosts
        hostIds.forEach(hostRepository::delete);

        log.info("Merged {} hosts into consolidated host {}", hostIds.size(), saved.getId());

        return saved;
    }

    /**
     * Simple merge strategy: Keep first host's IP and OS, collect all unique names/roles/protocols
     */
    private Host mergeHostsSimple(List<Host> hosts) {
        if (hosts.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge empty host list");
        }

        // Start with first host's basic info
        Host firstHost = hosts.get(0);
        String ipAddress = firstHost.getIpAddress();
        String os = firstHost.getOs();

        // Collect all unique names, roles, and protocols
        Set<String> allNames = new HashSet<>();
        Set<String> allRoles = new HashSet<>();
        Set<String> allProtocols = new HashSet<>();

        for (Host host : hosts) {
            // Add all names
            if (host.getNames() != null) {
                allNames.addAll(host.getNames());
            }

            // Add all roles
            if (host.getRoles() != null) {
                allRoles.addAll(host.getRoles());
            }

            // Add all protocols
            if (host.getProtocols() != null) {
                allProtocols.addAll(host.getProtocols());
            }

            // Use first non-null IP if current is null
            if (ipAddress == null && host.getIpAddress() != null) {
                ipAddress = host.getIpAddress();
            }

            // Use first non-null OS if current is null
            if (os == null && host.getOs() != null) {
                os = host.getOs();
            }
        }

        return Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .names(allNames)
                .roles(allRoles)
                .protocols(allProtocols)
                .os(os)
                .createdAt(LocalDateTime.now())
                .consolidated(true)
                .build();
    }

    /**
     * Deletes a host by ID
     */
    public void deleteHost(String id) {
        hostRepository.delete(id);
    }

    /**
     * Searches hosts by keyword - checks if keyword appears in any property
     */
    public List<HostGroup> searchHosts(String keyword) {
        List<HostGroup> allGroups = getAllHostGroups();

        if (keyword == null || keyword.trim().isEmpty()) {
            return allGroups;
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        return allGroups.stream()
                .filter(group -> matchesKeyword(group, lowerKeyword))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(HostGroup group, String keyword) {
        return group.getSimilarHosts().stream()
                .anyMatch(host -> hostMatchesKeyword(host, keyword));
    }

    private boolean hostMatchesKeyword(Host host, String keyword) {
        // Check IP address
        if (host.getIpAddress() != null &&
                host.getIpAddress().toLowerCase().contains(keyword)) {
            return true;
        }

        // Check names
        if (host.getNames() != null && host.getNames().stream()
                .anyMatch(name -> name.toLowerCase().contains(keyword))) {
            return true;
        }

        // Check roles
        if (host.getRoles() != null && host.getRoles().stream()
                .anyMatch(role -> role.toLowerCase().contains(keyword))) {
            return true;
        }

        // Check protocols
        if (host.getProtocols() != null && host.getProtocols().stream()
                .anyMatch(protocol -> protocol.toLowerCase().contains(keyword))) {
            return true;
        }

        // Check OS
        if (host.getOs() != null && host.getOs().toLowerCase().contains(keyword)) {
            return true;
        }

        return false;
    }
}