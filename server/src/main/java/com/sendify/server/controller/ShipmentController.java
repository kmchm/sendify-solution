package com.sendify.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sendify.server.client.DbSchenkerClient;
import com.sendify.server.dto.ShipmentDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final DbSchenkerClient dbSchenkerClient;

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDetails> getShipment(@PathVariable String id) {
        ShipmentDetails details = dbSchenkerClient.trackShipment(id);
        return ResponseEntity.ok(details);
    }
}