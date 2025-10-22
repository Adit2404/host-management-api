package com.hostmanagement.service;

import com.hostmanagement.dto.HostGroup;
import com.hostmanagement.model.Host;
import com.hostmanagement.repo.HostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HostServiceTest {

    @Mock
    private HostRepository hostRepository;

    @Mock
    private HostSimilarityService similarityService;

    @InjectMocks
    private HostService hostService;

    private Host testHost1;
    private Host testHost2;
    private Host consolidatedHost;

    @BeforeEach
    void setUp() {
        testHost1 = Host.builder()
                .id("host1")
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web01.example.com")))
                .roles(new HashSet<>(Arrays.asList("web_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP", "HTTPS")))
                .os("Ubuntu 22.04")
                .consolidated(false)
                .build();

        testHost2 = Host.builder()
                .id("host2")
                .ipAddress("192.168.1.10")
                .names(new HashSet<>(Arrays.asList("web01.example.com")))
                .roles(new HashSet<>(Arrays.asList("web_server")))
                .protocols(new HashSet<>(Arrays.asList("HTTP")))
                .os("Ubuntu 22.04")
                .consolidated(false)
                .build();

        consolidatedHost = Host.builder()
                .id("host3")
                .ipAddress("192.168.1.20")
                .names(new HashSet<>(Arrays.asList("db01.example.com")))
                .consolidated(true)
                .build();
    }

    @Test
    void testGetAllHostGroups_WithNonConsolidatedHosts() {
        // Arrange
        when(hostRepository.findAll()).thenReturn(Arrays.asList(testHost1, testHost2, consolidatedHost));

        Map<Host, List<Host>> similarGroups = new HashMap<>();
        similarGroups.put(testHost1, Arrays.asList(testHost1, testHost2));
        when(similarityService.groupSimilarHosts(anyList())).thenReturn(similarGroups);

        // Act
        List<HostGroup> groups = hostService.getAllHostGroups();

        // Assert
        assertNotNull(groups);
        assertEquals(2, groups.size());

        HostGroup nonConsolidatedGroup = groups.stream()
                .filter(g -> !g.isConsolidated())
                .findFirst()
                .orElse(null);

        assertNotNull(nonConsolidatedGroup);
        assertEquals(2, nonConsolidatedGroup.getTotalRecords());
        assertTrue(nonConsolidatedGroup.hasMultipleRecords());
    }

    @Test
    void testAddHost_Valid() {
        // Arrange
        when(hostRepository.save(any(Host.class))).thenReturn(testHost1);

        // Act
        Host result = hostService.addHost(testHost1);

        // Assert
        assertNotNull(result);
        assertEquals("192.168.1.10", result.getIpAddress());
        verify(hostRepository, times(1)).save(any(Host.class));
    }

    @Test
    void testAddHost_Invalid_ThrowsException() {
        // Arrange
        Host invalidHost = Host.builder()
                .id("invalid")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            hostService.addHost(invalidHost);
        });

        verify(hostRepository, never()).save(any(Host.class));
    }

    @Test
    void testMergeHosts_Success() {
        // Arrange
        List<String> hostIds = Arrays.asList("host1", "host2");
        when(hostRepository.findById("host1")).thenReturn(Optional.of(testHost1));
        when(hostRepository.findById("host2")).thenReturn(Optional.of(testHost2));
        when(hostRepository.save(any(Host.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Host merged = hostService.mergeHosts(hostIds);

        // Assert
        assertNotNull(merged);
        assertTrue(merged.isConsolidated());
        assertEquals("192.168.1.10", merged.getIpAddress());
        assertTrue(merged.getProtocols().contains("HTTP"));
        assertTrue(merged.getProtocols().contains("HTTPS"));

        verify(hostRepository, times(1)).save(any(Host.class));
        verify(hostRepository, times(2)).delete(anyString());
    }

    @Test
    void testMergeHosts_EmptyList_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            hostService.mergeHosts(new ArrayList<>());
        });

        verify(hostRepository, never()).save(any(Host.class));
    }

    @Test
    void testMergeHosts_InvalidIds_ThrowsException() {
        // Arrange
        List<String> hostIds = Arrays.asList("nonexistent1", "nonexistent2");
        when(hostRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            hostService.mergeHosts(hostIds);
        });

        verify(hostRepository, never()).save(any(Host.class));
    }

    @Test
    void testGetHostById_Found() {
        // Arrange
        when(hostRepository.findById("host1")).thenReturn(Optional.of(testHost1));

        // Act
        Optional<Host> result = hostService.getHostById("host1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("host1", result.get().getId());
    }

    @Test
    void testGetHostById_NotFound() {
        // Arrange
        when(hostRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<Host> result = hostService.getHostById("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testSearchHosts_WithKeyword() {
        // Arrange
        when(hostRepository.findAll()).thenReturn(Arrays.asList(testHost1, testHost2));

        Map<Host, List<Host>> similarGroups = new HashMap<>();
        similarGroups.put(testHost1, Arrays.asList(testHost1, testHost2));
        when(similarityService.groupSimilarHosts(anyList())).thenReturn(similarGroups);

        // Act
        List<HostGroup> results = hostService.searchHosts("web01");

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testDeleteHost() {
        // Act
        hostService.deleteHost("host1");

        // Assert
        verify(hostRepository, times(1)).delete("host1");
    }
}