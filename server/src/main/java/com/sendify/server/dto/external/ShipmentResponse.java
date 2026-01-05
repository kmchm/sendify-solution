package com.sendify.server.dto.external;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentResponse {

    @JsonProperty("result")
    private List<ShipmentResult> results;

    @JsonProperty("warnings")
    private List<String> warnings;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShipmentResult {

        private String id;
        private String stt;
        private String transportMode;
        private Integer percentageProgress;
        private String lastEventCode;

        private String fromLocation;
        private String toLocation;

        private String startDate;
        private String endDate;

        private String consignment;
        private String additionalReferenceValues;
    }
}
