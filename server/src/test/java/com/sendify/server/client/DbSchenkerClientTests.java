package com.sendify.server.client;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sendify.server.dto.internal.ShipmentDetailsDto;

@SpringBootTest
@Disabled
class DbSchenkerClientTests {

    @Autowired
    private DbSchenkerClient dbSchenkerClient;

    private final List<String> trackingIds = List.of(
        "1806203236", "1806290829", "1806273700", "1806272330", "1806271886", "1806270433",
        "1806268072", "1806267579", "1806264568", "1806258974", "1806256390"
    );

    @Test
    void noErrorForTestIds() {
        for (String trackingId : trackingIds) {
            assertThatCode(() -> {
                ShipmentDetailsDto dto = dbSchenkerClient.trackShipment(trackingId);
                assert dto != null;
            }).doesNotThrowAnyException();
        }
    }
}