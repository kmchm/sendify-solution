package com.sendify.server.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sendify.server.client.DbSchenkerClient;
import com.sendify.server.dto.ShipmentDetails;

class ShipmentControllerTest {

    @Mock
    private DbSchenkerClient dbSchenkerClient;

    @InjectMocks
    private ShipmentController shipmentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1806203236",
            "1806290829",
            "1806273700",
            "1806272330",
            "1806271886",
            "1806270433",
            "1806268072",
            "1806267579",
            "1806264568",
            "1806258974",
            "1806256390"
    })
    void testGetShipment_ReturnsShipmentDetails(String id) {
        ShipmentDetails expected = expectedDetailsForId(id);
        when(dbSchenkerClient.trackShipment(id)).thenReturn(expected);

        ResponseEntity<ShipmentDetails> response = shipmentController.getShipment(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ShipmentDetails actual = response.getBody();
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getStt(), actual.getStt());
        assertEquals(expected.getTransportMode(), actual.getTransportMode());
        assertEquals(expected.getPercentageProgress(), actual.getPercentageProgress());
        assertEquals(expected.getLastEventCode(), actual.getLastEventCode());
        assertEquals(expected.getFromLocation(), actual.getFromLocation());
        assertEquals(expected.getToLocation(), actual.getToLocation());
        assertEquals(expected.getStartDate(), actual.getStartDate());
        assertEquals(expected.getEndDate(), actual.getEndDate());
        verify(dbSchenkerClient, times(1)).trackShipment(id);
    }

    private ShipmentDetails expectedDetailsForId(String id) {
        ShipmentDetails d = new ShipmentDetails();
        switch (id) {
            case "1806203236":
                d.setId("LandStt:SENYB550963155");
                d.setStt("SENYB550963155");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Nybro");
                d.setToLocation("Tranås");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-11T00:00:00Z");
                break;
            case "1806290829":
                d.setId("LandStt:VAN5022058");
                d.setStt("VAN5022058");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Sjuntorp");
                d.setToLocation("Dourges");
                d.setStartDate("2025-12-11T00:00:00Z");
                d.setEndDate("2025-12-18T00:00:00Z");
                break;
            case "1806273700":
                d.setId("LandStt:SENYB550963616");
                d.setStt("SENYB550963616");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Nybro");
                d.setToLocation("Stockholm");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-12T00:00:00Z");
                break;
            case "1806272330":
                d.setId("LandStt:SENYB550963621");
                d.setStt("SENYB550963621");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Alstermo");
                d.setToLocation("Trollhättan");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-12T00:00:00Z");
                break;
            case "1806271886":
                d.setId("LandStt:SENYB550963630");
                d.setStt("SENYB550963630");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Nybro");
                d.setToLocation("Stockholm");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-15T00:00:00Z");
                break;
            case "1806270433":
                d.setId("LandStt:SEGOT550016710");
                d.setStt("SEGOT550016710");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Hisings backa");
                d.setToLocation("Örebro");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-12T00:00:00Z");
                break;
            case "1806268072":
                d.setId("LandStt:SEGOT550016727");
                d.setStt("SEGOT550016727");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Hisings backa");
                d.setToLocation("Kågeröd");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-12T00:00:00Z");
                break;
            case "1806267579":
                d.setId("LandStt:SEJKG550511058");
                d.setStt("SEJKG550511058");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Eksjö");
                d.setToLocation("Borlänge");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-12T00:00:00Z");
                break;
            case "1806264568":
                d.setId("LandStt:SENYB550963561");
                d.setStt("SENYB550963561");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Nybro");
                d.setToLocation("Kalmar");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-12T00:00:00Z");
                break;
            case "1806258974":
                d.setId("LandStt:SEBOS550183974");
                d.setStt("SEBOS550183974");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Skene");
                d.setToLocation("Stockholm");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-15T00:00:00Z");
                break;
            case "1806256390":
                d.setId("LandStt:SENYB550963479");
                d.setStt("SENYB550963479");
                d.setTransportMode("LAND");
                d.setPercentageProgress(100);
                d.setLastEventCode("DLV");
                d.setFromLocation("Nybro");
                d.setToLocation("Jönköping");
                d.setStartDate("2025-12-10T00:00:00Z");
                d.setEndDate("2025-12-12T00:00:00Z");
                break;
        }
        return d;
    }
}