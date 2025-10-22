package com.hostmanagement.service;

import com.hostmanagement.model.Host;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class HostSimilarityServiceTest {

    private HostSimilarityService similarityService;

    @BeforeEach
    void setUp() {
        similarityService = new HostSimilarityService();
    }

    @Test
    void testCalculateSimilarity_IdenticalHosts_Returns100() {
        // Arrange
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .roles(new HashSet<>(Arrays.asList("web_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP", "HTTPS")))
                .os("Ubuntu 22.04")
                .build();

        Host host2 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .roles(new HashSet<>(Arrays.asList("web_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP", "HTTPS")))
                .os("Ubuntu 22.04")
                .build();

        // Act
        int similarity = similarityService.calculateSimilarity(host1, host2);

        // Assert
        assertEquals(100, similarity); // IP(40) + Name(30) + Role(15) + Protocol(10) + OS(5) = 100
    }

    @Test
    void testCalculateSimilarity_SameIPOnly_Returns40() {
        // Arrange
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .build();

        Host host2 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("api.example.com")))
                .build();

        // Act
        int similarity = similarityService.calculateSimilarity(host1, host2);

        // Assert
        assertEquals(40, similarity); // Only IP matches
    }

    @Test
    void testCalculateSimilarity_SameIPAndName_Returns70() {
        // Arrange
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .build();

        Host host2 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com", "api.example.com")))
                .build();

        // Act
        int similarity = similarityService.calculateSimilarity(host1, host2);

        // Assert
        assertEquals(70, similarity); // IP(40) + Name(30) = 70
    }

    @Test
    void testCalculateSimilarity_DifferentEverything_Returns0() {
        // Arrange
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .roles(new HashSet<>(Arrays.asList("web_server")))
                .build();

        Host host2 = Host.builder()
                .ipAddress("10.0.0.50")
                .names(new HashSet<>(Arrays.asList("mail.other.com")))
                .roles(new HashSet<>(Arrays.asList("mail_server")))
                .build();

        // Act
        int similarity = similarityService.calculateSimilarity(host1, host2);

        // Assert
        assertEquals(0, similarity); // Nothing matches
    }

    @Test
    void testCalculateSimilarity_SameOSFamily_Returns45() {
        // Arrange
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .os("Ubuntu 20.04")
                .build();

        Host host2 = Host.builder()
                .ipAddress("192.168.1.10")
                .os("Ubuntu 22.04")
                .build();

        // Act
        int similarity = similarityService.calculateSimilarity(host1, host2);

        // Assert
        assertEquals(45, similarity); // IP(40) + OS(5) = 45
    }

    @Test
    void testAreSimilar_ScoreAbove50_ReturnsTrue() {
        // Arrange - IP + Name = 70 points (above threshold)
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .build();

        Host host2 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .build();

        // Act
        boolean areSimilar = similarityService.areSimilar(host1, host2);

        // Assert
        assertTrue(areSimilar);
    }

    @Test
    void testAreSimilar_ScoreBelow50_ReturnsFalse() {
        // Arrange - Only IP = 40 points (below threshold of 50)
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .build();

        Host host2 = Host.builder()
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("api.example.com")))
                .build();

        // Act
        boolean areSimilar = similarityService.areSimilar(host1, host2);

        // Assert
        assertFalse(areSimilar);
    }

    @Test
    void testAreSimilar_ExactThreshold_ReturnsTrue() {
        // Arrange - IP + Protocol = 50 points (exactly at threshold)
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .protocols(new HashSet<>(Arrays.asList("HTTP")))
                .build();

        Host host2 = Host.builder()
                .ipAddress("192.168.1.10")
                .protocols(new HashSet<>(Arrays.asList("HTTP", "HTTPS")))
                .build();

        // Act
        boolean areSimilar = similarityService.areSimilar(host1, host2);

        // Assert
        assertTrue(areSimilar);
    }

    @Test
    void testGroupSimilarHosts_GroupsSimilarHostsTogether() {
        // Arrange
        Host host1 = Host.builder()
                .id("1")
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .build();

        Host host2 = Host.builder()
                .id("2")
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .build();

        Host host3 = Host.builder()
                .id("3")
                .ipAddress("10.0.0.50")
                .names(new HashSet<>(Arrays.asList("mail.other.com")))
                .build();

        List<Host> hosts = Arrays.asList(host1, host2, host3);

        // Act
        Map<Host, List<Host>> groups = similarityService.groupSimilarHosts(hosts);

        // Assert
        assertEquals(2, groups.size()); // 2 groups: {host1, host2} and {host3}

        // Verify host1 and host2 are in the same group
        boolean host1And2Grouped = groups.values().stream()
                .anyMatch(group -> group.contains(host1) && group.contains(host2));
        assertTrue(host1And2Grouped);
    }

    @Test
    void testGroupSimilarHosts_NoSimilarHosts_EachInOwnGroup() {
        // Arrange
        Host host1 = Host.builder()
                .id("1")
                .ipAddress("192.168.1.10")
                .build();

        Host host2 = Host.builder()
                .id("2")
                .ipAddress("192.168.1.20")
                .build();

        Host host3 = Host.builder()
                .id("3")
                .ipAddress("192.168.1.30")
                .build();

        List<Host> hosts = Arrays.asList(host1, host2, host3);

        // Act
        Map<Host, List<Host>> groups = similarityService.groupSimilarHosts(hosts);

        // Assert
        assertEquals(3, groups.size()); // Each host in its own group
        groups.values().forEach(group -> assertEquals(1, group.size()));
    }

    @Test
    void testCalculateSimilarity_NameAndRole_Returns45() {
        Host host1 = Host.builder()
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .roles(new HashSet<>(Arrays.asList("web_server")))
                .build();

        Host host2 = Host.builder()
                .names(new HashSet<>(Arrays.asList("web.example.com")))
                .roles(new HashSet<>(Arrays.asList("web_server")))
                .build();

        // Act
        int similarity = similarityService.calculateSimilarity(host1, host2);

        // Assert
        assertEquals(45, similarity);
    }

    @Test
    void testCalculateSimilarity_PartialMatches_AddsUpCorrectly() {
        // Arrange
        Host host1 = Host.builder()
                .ipAddress("192.168.1.10")
                .roles(new HashSet<>(Arrays.asList("web_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP")))
                .build();

        Host host2 = Host.builder()
                .ipAddress("192.168.1.10")
                .roles(new HashSet<>(Arrays.asList("web_server", "api_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP", "HTTPS")))
                .build();

        // Act
        int similarity = similarityService.calculateSimilarity(host1, host2);

        // Assert
        assertEquals(65, similarity);
    }
}