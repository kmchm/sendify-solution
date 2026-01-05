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
public class TripResponse {

    private String start;
    private String end;

    @JsonProperty("trip")
    private List<TripPoint> tripPoints;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TripPoint {

        private String lastEventCode;
        private String lastEventDate;
        private Double latitude;
        private Double longitude;
    }
}
