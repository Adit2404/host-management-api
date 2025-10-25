package com.hostmanagement.repo;

import com.hostmanagement.model.Host;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for Host entities.
 * Uses ConcurrentHashMap for thread-safe operations.
 */
@Repository
public class HostRepository {

    private final Map<String, Host> hosts = new ConcurrentHashMap<>();
    private static final String HOST_OS = "Ubuntu 22.04";
    private static final String HOST_IP = "192.168.1.10";
    private static final String HOST_PROTOCOL = "HTTPS";
    private static final String HOST_ROLE_WS = "web_server";
    /**
     * Initializes the repository with sample data
     */
    @PostConstruct
    public void initializeData() {
      saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress(HOST_IP)
                .names(new HashSet<>(Arrays.asList("web01.example.com", "www.example.com")))
                .roles(new HashSet<>(Arrays.asList(HOST_ROLE_WS, "application_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP", HOST_PROTOCOL, "SSH")))
                .os(HOST_OS)
                .createdAt(LocalDateTime.now().minusDays(5))
                .consolidated(false)
                .build());

        saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress(HOST_IP)
                .names(new HashSet<>(Arrays.asList("web01.example.com", "api.example.com")))
                .roles(new HashSet<>(Arrays.asList(HOST_ROLE_WS, "api_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP", HOST_PROTOCOL)))
                .os(HOST_OS)
                .createdAt(LocalDateTime.now().minusDays(3))
                .consolidated(false)
                .build());

        saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress(HOST_IP)
                .names(new HashSet<>(Collections.singletonList("www.example.com")))
                .roles(new HashSet<>(Collections.singletonList(HOST_ROLE_WS)))
                .protocols(new HashSet<>(Arrays.asList("HTTP", HOST_PROTOCOL, "FTP")))
                .os("Ubuntu 22")
                .createdAt(LocalDateTime.now().minusDays(1))
                .consolidated(false)
                .build());

        // Similar hosts - Database servers
        saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress("192.168.1.20")
                .names(new HashSet<>(Arrays.asList("db01.example.com", "database.example.com")))
                .roles(new HashSet<>(Arrays.asList("database_server", "data_storage")))
                .protocols(new HashSet<>(Arrays.asList("MySQL", "SSH")))
                .os("CentOS 8")
                .createdAt(LocalDateTime.now().minusDays(4))
                .consolidated(false)
                .build());

        saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress("192.168.1.20")
                .names(new HashSet<>(Collections.singletonList("db01.example.com")))
                .roles(new HashSet<>(Collections.singletonList("database_server")))
                .protocols(new HashSet<>(Arrays.asList("MySQL", "PostgreSQL", "SSH")))
                .os("CentOS 8")
                .createdAt(LocalDateTime.now().minusDays(2))
                .consolidated(false)
                .build());

        // Mail server
        saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress("192.168.1.30")
                .names(new HashSet<>(Arrays.asList("mail.example.com", "smtp.example.com")))
                .roles(new HashSet<>(Arrays.asList("mail_server", "smtp_server")))
                .protocols(new HashSet<>(Arrays.asList("SMTP", "IMAP", "POP3")))
                .os("Windows Server 2019")
                .createdAt(LocalDateTime.now().minusDays(6))
                .consolidated(false)
                .build());

        // DNS server (unique)
        saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress("192.168.1.40")
                .names(new HashSet<>(Collections.singletonList("dns01.example.com")))
                .roles(new HashSet<>(Collections.singletonList("dns_server")))
                .protocols(new HashSet<>(Arrays.asList("DNS", "SSH")))
                .os("Ubuntu 20.04")
                .createdAt(LocalDateTime.now().minusDays(7))
                .consolidated(false)
                .build());

        // Print server
        saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress("192.168.1.50")
                .names(new HashSet<>(Collections.singletonList("printer01.example.com")))
                .roles(new HashSet<>(Collections.singletonList("printer")))
                .protocols(new HashSet<>(Arrays.asList("IPP", "LPD")))
                .os("Printer Firmware v2.3")
                .createdAt(LocalDateTime.now().minusDays(10))
                .consolidated(false)
                .build());

        // File server with IPv6
        saveInitial(Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                .names(new HashSet<>(Arrays.asList("files.example.com", "storage.example.com")))
                .roles(new HashSet<>(Arrays.asList("file_server", "storage_server")))
                .protocols(new HashSet<>(Arrays.asList("SMB", "NFS", "FTP", "SSH")))
                .os("Windows Server 2022")
                .createdAt(LocalDateTime.now().minusDays(3))
                .consolidated(false)
                .build());

        // Already consolidated host
        Host consolidatedHost = Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress("192.168.1.100")
                .names(new HashSet<>(Arrays.asList("app01.example.com", "application.example.com")))
                .roles(new HashSet<>(Arrays.asList("application_server", "backend_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP", HOST_PROTOCOL, "gRPC")))
                .os(HOST_OS)
                .createdAt(LocalDateTime.now().minusDays(15))
                .consolidated(true)
                .sourceRecordIds(new HashSet<>(Arrays.asList("source-1", "source-2")))
                .build();
        saveInitial(consolidatedHost);
    }

    private void saveInitial(Host host) {
        hosts.put(host.getId(), host);
    }

    public List<Host> findAll() {
        return new ArrayList<>(hosts.values());
    }

    public Optional<Host> findById(String id) {
        return Optional.ofNullable(hosts.get(id));
    }

    public Host save(Host host) {
        hosts.put(host.getId(), host);
        return host;
    }

    public void delete(String id) {
        hosts.remove(id);
    }

    public void deleteAll() {
        hosts.clear();
    }

    public long count() {
        return hosts.size();
    }
}