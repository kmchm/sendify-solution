package com.sendify.server.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sendify.server.dto.external.LandSttResponse;
import com.sendify.server.dto.internal.ShipmentDetailsDto;

@Component
public class LandSttResponseMapper {

    public ShipmentDetailsDto map(LandSttResponse landSttResponse) {
        ShipmentDetailsDto.Party sender = ShipmentDetailsDto.Party.builder()
                .name(String.valueOf(landSttResponse.getReferences().getShipper()))
                .address(
                        ShipmentDetailsDto.Address.builder()
                                .countryCode(landSttResponse.getLocation().getShipperPlace().getCountryCode())
                                .country(landSttResponse.getLocation().getShipperPlace().getCountry())
                                .city(landSttResponse.getLocation().getShipperPlace().getCity())
                                .postCode(landSttResponse.getLocation().getShipperPlace().getPostCode())
                                .build()
                )
                .build();

        ShipmentDetailsDto.Party receiver = ShipmentDetailsDto.Party.builder()
                .name(String.valueOf(landSttResponse.getReferences().getConsignee()))
                .address(
                        ShipmentDetailsDto.Address.builder()
                                .countryCode(landSttResponse.getLocation().getConsigneePlace().getCountryCode())
                                .country(landSttResponse.getLocation().getConsigneePlace().getCountry())
                                .city(landSttResponse.getLocation().getConsigneePlace().getCity())
                                .postCode(landSttResponse.getLocation().getConsigneePlace().getPostCode())
                                .build()
                )
                .build();

        ShipmentDetailsDto.PackageDetails packageDetails = ShipmentDetailsDto.PackageDetails.builder()
                .pieceCount(landSttResponse.getGoods().getPieces())
                .weight(landSttResponse.getGoods().getWeight().getValue())
                .weightUnit(landSttResponse.getGoods().getWeight().getUnit())
                .dimensions(null)
                .build();

        List<ShipmentDetailsDto.TrackingEvent> trackingHistory = landSttResponse.getEvents().stream()
                .map(event -> ShipmentDetailsDto.TrackingEvent.builder()
                        .code(event.getCode())
                        .date(event.getDate())
                        .location(event.getLocation() != null ? event.getLocation().getName() : null)
                        .comment(event.getComment())
                        .build())
                .toList();

        List<ShipmentDetailsDto.PackageTracking> packageTracking = landSttResponse.getPackages().stream()
                .map(pkg -> ShipmentDetailsDto.PackageTracking.builder()
                        .packageId(pkg.getId())
                        .events(pkg.getEvents().stream()
                                .map(event -> ShipmentDetailsDto.TrackingEvent.builder()
                                        .code(event.getCode())
                                        .date(event.getDate())
                                        .location(event.getLocation() != null ? event.getLocation() : null)
                                        .comment(event.getComment())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return ShipmentDetailsDto.builder()
                .sender(sender)
                .receiver(receiver)
                .packageDetails(packageDetails)
                .trackingHistory(trackingHistory)
                .packageTracking(packageTracking)
                .build();
    }
}