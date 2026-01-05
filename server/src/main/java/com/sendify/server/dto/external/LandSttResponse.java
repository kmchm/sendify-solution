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
public class LandSttResponse {

    @JsonProperty("sttNumber")
    private String sttNumber;

    @JsonProperty("product")
    private String product;

    @JsonProperty("references")
    private References references;

    @JsonProperty("goods")
    private Goods goods;

    @JsonProperty("location")
    private NetworkLocation location;

    @JsonProperty("events")
    private List<TrackingEvent> events;

    @JsonProperty("packages")
    private List<PackageItem> packages;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class References {

        private List<String> shipper;
        private List<String> consignee;
        private List<String> waybillAndConsignementNumbers;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Goods {

        private Integer pieces;
        private Measurement weight;
        private Measurement volume;
        private Measurement loadingMeters;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Measurement {

        private Double value;
        private String unit;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NetworkLocation {

        private Address collectFrom;
        private Address deliverTo;
        private Address shipperPlace;
        private Address consigneePlace;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {

        private String country;
        private String countryCode;
        private String city;
        private String postCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrackingEvent {

        private String code;
        private String date;
        private String comment;
        private EventLocation location;
        private List<Reason> reasons;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventLocation {

        private String name;
        private String code;
        private String countryCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reason {

        private String code;
        private String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackageItem {

        private String id;
        private List<PackageEvent> events;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackageEvent {

        private String code;
        private String date;
        private String location;
        private String countryCode;
    }
}
