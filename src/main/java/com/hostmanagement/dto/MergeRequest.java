package com.hostmanagement.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for merging multiple host records into a consolidated host.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MergeRequest {

    @NotEmpty(message = "Host IDs to merge cannot be empty")
    private List<String> hostIds;
}