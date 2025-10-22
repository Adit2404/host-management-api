package com.hostmanagement.service;

import com.hostmanagement.model.Host;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for computing similarity between host records using a simple weighted scoring system.
 *
 * Simple Similarity Heuristic (Easy to understand and maintain):
 *
 * 1. IP Address Match (40 points): Same IP = definitely same host
 * 2. Name Match (30 points): Any common domain name = likely same host
 * 3. Role Match (15 points): Any common role = moderately similar
 * 4. Protocol Match (10 points): Any common protocol = slightly similar
 * 5. OS Match (5 points): Same OS = bonus similarity
 *
 * Total possible score: 100 points
 * Threshold: 50 points (50%) - hosts scoring 50+ are considered similar
 *
 * This approach minimizes false positives by requiring at least:
 * - IP match alone (40 pts) + any other match (10+ pts), OR
 * - Name match (30 pts) + Role match (15 pts) + something else
 */
@Service
public class HostSimilarityService {

    private static final int IP_MATCH_SCORE = 40;
    private static final int NAME_MATCH_SCORE = 30;
    private static final int ROLE_MATCH_SCORE = 15;
    private static final int PROTOCOL_MATCH_SCORE = 10;
    private static final int OS_MATCH_SCORE = 5;
    private static final int SIMILARITY_THRESHOLD = 50;

    /**
     * Calculates similarity score between two hosts (0 to 100)
     * Simple scoring: if properties match, add points
     */
    public int calculateSimilarity(Host host1, Host host2) {
        int score = 0;

        // Check IP address match (40 points)
        if (hasIpMatch(host1, host2)) {
            score += IP_MATCH_SCORE;
        }

        // Check if any names match (30 points)
        if (hasAnyNameMatch(host1, host2)) {
            score += NAME_MATCH_SCORE;
        }

        // Check if any roles match (15 points)
        if (hasAnyRoleMatch(host1, host2)) {
            score += ROLE_MATCH_SCORE;
        }

        // Check if any protocols match (10 points)
        if (hasAnyProtocolMatch(host1, host2)) {
            score += PROTOCOL_MATCH_SCORE;
        }

        // Check OS match (5 points)
        if (hasOsMatch(host1, host2)) {
            score += OS_MATCH_SCORE;
        }

        return score;
    }

    /**
     * Determines if two hosts are similar enough to be grouped
     */
    public boolean areSimilar(Host host1, Host host2) {
        return calculateSimilarity(host1, host2) >= SIMILARITY_THRESHOLD;
    }

    /**
     * Checks if two hosts have the same IP address
     */
    private boolean hasIpMatch(Host host1, Host host2) {
        if (host1.getIpAddress() == null || host2.getIpAddress() == null) {
            return false;
        }
        return host1.getIpAddress().equals(host2.getIpAddress());
    }

    /**
     * Checks if two hosts share at least one domain name
     */
    private boolean hasAnyNameMatch(Host host1, Host host2) {
        if (host1.getNames() == null || host2.getNames() == null) {
            return false;
        }
        if (host1.getNames().isEmpty() || host2.getNames().isEmpty()) {
            return false;
        }

        // Check if any name from host1 exists in host2
        for (String name : host1.getNames()) {
            if (host2.getNames().contains(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if two hosts share at least one role
     */
    private boolean hasAnyRoleMatch(Host host1, Host host2) {
        if (host1.getRoles() == null || host2.getRoles() == null) {
            return false;
        }
        if (host1.getRoles().isEmpty() || host2.getRoles().isEmpty()) {
            return false;
        }

        for (String role : host1.getRoles()) {
            if (host2.getRoles().contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if two hosts share at least one protocol
     */
    private boolean hasAnyProtocolMatch(Host host1, Host host2) {
        if (host1.getProtocols() == null || host2.getProtocols() == null) {
            return false;
        }
        if (host1.getProtocols().isEmpty() || host2.getProtocols().isEmpty()) {
            return false;
        }

        for (String protocol : host1.getProtocols()) {
            if (host2.getProtocols().contains(protocol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if two hosts have the same operating system
     * Also considers partial matches (e.g., "Ubuntu 20" matches "Ubuntu 22")
     */
    private boolean hasOsMatch(Host host1, Host host2) {
        if (host1.getOs() == null || host2.getOs() == null) {
            return false;
        }

        String os1 = host1.getOs().toLowerCase();
        String os2 = host2.getOs().toLowerCase();

        // Exact match
        if (os1.equals(os2)) {
            return true;
        }

        // Check if they share the same OS family (first word)
        String[] parts1 = os1.split("\\s+");
        String[] parts2 = os2.split("\\s+");

        if (parts1.length > 0 && parts2.length > 0) {
            return parts1[0].equals(parts2[0]);
        }

        return false;
    }

    /**
     * Groups similar hosts together
     * Returns a map where key is the first host in group and value is all similar hosts
     */
    public Map<Host, List<Host>> groupSimilarHosts(List<Host> hosts) {
        Map<Host, List<Host>> groups = new HashMap<>();
        Set<Host> processed = new HashSet<>();

        for (Host host : hosts) {
            if (processed.contains(host)) {
                continue;
            }

            List<Host> similarGroup = new ArrayList<>();
            similarGroup.add(host);
            processed.add(host);

            // Find all similar hosts
            for (Host candidate : hosts) {
                if (!processed.contains(candidate) && areSimilar(host, candidate)) {
                    similarGroup.add(candidate);
                    processed.add(candidate);
                }
            }

            groups.put(host, similarGroup);
        }

        return groups;
    }
}