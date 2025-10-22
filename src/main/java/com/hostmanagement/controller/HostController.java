package com.hostmanagement.controller;

import com.hostmanagement.dto.HostGroup;
import com.hostmanagement.dto.MergeRequest;
import com.hostmanagement.model.Host;
import com.hostmanagement.service.HostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Minimal REST API controller with cohesive endpoints for host management.
 *
 * Endpoint Design Philosophy:
 * - Keep it simple with minimal endpoints
 * - Each endpoint has a clear, single purpose
 * - Follow REST conventions
 */
@Slf4j
@RestController
@RequestMapping("/api/hosts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Host Management", description = "API for managing and consolidating host records")
public class HostController {

    private final HostService hostService;

    /**
     * Endpoint 1: GET /api/hosts/groups
     * Purpose: List all host groups with optional search
     * Used by: Main view to display all hosts
     */
    @Operation(summary = "Get all host groups",
            description = "Lists all hosts grouped by similarity. Supports optional search parameter.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved host groups")
    @GetMapping("/groups")
    public ResponseEntity<List<HostGroup>> getAllHostGroups(
            @Parameter(description = "Optional search keyword to filter hosts")
            @RequestParam(required = false) String search) {

        log.info("GET /api/hosts/groups - search: {}", search);

        List<HostGroup> groups = search != null && !search.trim().isEmpty()
                ? hostService.searchHosts(search)
                : hostService.getAllHostGroups();

        return ResponseEntity.ok(groups);
    }

    /**
     * Endpoint 2: GET /api/hosts/groups/{groupId}
     * Purpose: Get details of a specific group
     * Used by: Detail view to show group information
     */
    @Operation(summary = "Get host group by ID",
            description = "Retrieves details of a specific host group including all similar records")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved host group")
    @ApiResponse(responseCode = "404", description = "Host group not found")
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<HostGroup> getHostGroup(
            @Parameter(description = "ID of the host group")
            @PathVariable String groupId) {

        log.info("GET /api/hosts/groups/{}", groupId);

        return hostService.getHostGroup(groupId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint 3: POST /api/hosts/merge
     * Purpose: Merge multiple hosts into one consolidated record
     * Used by: Detail view when user confirms merge operation
     */
    @Operation(summary = "Merge hosts",
            description = "Merges selected host records into a single consolidated host. " +
                    "Original records are deleted and replaced with the merged host.")
    @ApiResponse(responseCode = "200", description = "Hosts merged successfully")
    @ApiResponse(responseCode = "400", description = "Invalid merge request")
    @PostMapping("/merge")
    public ResponseEntity<Host> mergeHosts(@Valid @RequestBody MergeRequest request) {
        log.info("POST /api/hosts/merge - Merging {} hosts", request.getHostIds().size());

        try {
            Host merged = hostService.mergeHosts(request.getHostIds());
            return ResponseEntity.ok(merged);
        } catch (IllegalArgumentException e) {
            log.error("Merge failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    @Operation(summary = "Add a new host record",
            description = "Creates a new host record with the provided properties")
    @ApiResponse(responseCode = "201", description = "Host created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid host data")
    @PostMapping
    public ResponseEntity<Host> addHost(@Valid @RequestBody Host host) {
        log.info("POST /api/hosts - Adding new host");

        try {
            Host created = hostService.addHost(host);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Invalid host data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}