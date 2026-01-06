package com.sendify.server.dto.internal;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipmentDetailsDto {
    private Party sender;
    private Party receiver;
    private PackageDetails packageDetails;
    private List<TrackingEvent> trackingHistory;
    private List<PackageTracking> packageTracking;

    @Data
    @Builder
    public static class Party {
        private String name;
        private Address address;
    }

    @Data
    @Builder
    public static class Address {
        private String countryCode;
        private String country;
        private String city;
        private String postCode;
    }

    @Data
    @Builder
    public static class PackageDetails {
        private int pieceCount;
        private double weight;
        private String weightUnit;
        private List<Dimension> dimensions;
    }

    @Data
    @Builder
    public static class Dimension {
        private Double length;
        private Double width;
        private Double height;
        private String unit;
    }

    @Data
    @Builder
    public static class TrackingEvent {
        private String code;
        private String date;
        private String location;
        private String comment;
    }

    @Data
    @Builder
    public static class PackageTracking {
        private String packageId;
        private List<TrackingEvent> events;
    }
}