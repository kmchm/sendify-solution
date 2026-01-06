package com.sendify.server.mcp;

import com.sendify.server.client.DbSchenkerClient;
import com.sendify.server.dto.internal.ShipmentDetailsDto;
import org.springframework.stereotype.Service;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Service
public class ShipmentTool {
    private final DbSchenkerClient dbSchenkerClient;

    public ShipmentTool(DbSchenkerClient dbSchenkerClient) {
        this.dbSchenkerClient = dbSchenkerClient;
    }


    @Tool(
            name = "Tracks a shipment using DB Schenker reference number.",
            description = """
                    Tracks a shipment using DB Schenker reference number.
                    Returns
                    sender information,
                    receiver information,
                    package details,
                    complete tracking history for the shipment,
                    and
                    individual tracking events per package.
                    """
    )
    public ShipmentDetailsDto trackShipment(
            @ToolParam(description = "The shipment reference number (e.g., 1806203236)") String referenceNumber
    ) {
        return dbSchenkerClient.trackShipment(referenceNumber);
    }
}
