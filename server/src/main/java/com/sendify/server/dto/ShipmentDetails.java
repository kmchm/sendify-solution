package com.sendify.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentDetails {
    private String id;
    private String stt;
    private String transportMode;
    private int percentageProgress;
    private String lastEventCode;
    private String fromLocation;
    private String toLocation;
    private String startDate;
    private String endDate;
}