package com.hostmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a host record with network properties.
 * Each host can have multiple properties that identify it on the network.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Host {

    private String id;

    @JsonProperty("ip_address")
    private String ipAddress;

    @Builder.Default
    private Set<String> names = new HashSet<>();

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private Set<String> protocols = new HashSet<>();

    private String os;

    private LocalDateTime createdAt;

    private boolean consolidated;

    @Builder.Default
    private Set<String> sourceRecordIds = new HashSet<>();

    /**
     * Validates that at least one property is non-empty
     */
    public boolean isValid() {
        return (ipAddress != null && !ipAddress.isEmpty()) ||
                (names != null && !names.isEmpty()) ||
                (roles != null && !roles.isEmpty()) ||
                (protocols != null && !protocols.isEmpty()) ||
                (os != null && !os.isEmpty());
    }

    /**
     * Creates a new host with a generated ID and timestamp
     */
    public static Host create(String ipAddress, Set<String> names, Set<String> roles,
                              Set<String> protocols, String os) {
        return Host.builder()
                .id(UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .names(names != null ? new HashSet<>(names) : new HashSet<>())
                .roles(roles != null ? new HashSet<>(roles) : new HashSet<>())
                .protocols(protocols != null ? new HashSet<>(protocols) : new HashSet<>())
                .os(os)
                .createdAt(LocalDateTime.now())
                .consolidated(false)
                .sourceRecordIds(new HashSet<>())
                .build();
    }
}