package com.sendify.server.controller;

import com.sendify.server.dto.internal.ShipmentDetailsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sendify.server.client.DbSchenkerClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final DbSchenkerClient dbSchenkerClient;

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDetailsDto> getShipment(@PathVariable String id) {
        ShipmentDetailsDto details = dbSchenkerClient.trackShipment(id);
        return ResponseEntity.ok(details);
    }
}