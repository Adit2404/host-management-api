package com.hostmanagement.dto;

import com.hostmanagement.model.Host;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of similar host records that can be consolidated.
 * Used for displaying non-consolidated hosts in the main view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostGroup {

    private String groupId;
    private Host primaryHost;

    @Builder.Default
    private List<Host> similarHosts = new ArrayList<>();

    private int totalRecords;
    private boolean consolidated;
    private double similarityScore;

    /**
     * Gets the total number of hosts in the group including primary
     */
    public int getTotalRecords() {
        return similarHosts.size();
    }

    /**
     * Checks if this group has multiple records
     */
    public boolean hasMultipleRecords() {
        return similarHosts.size() > 1;
    }
}
