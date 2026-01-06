package com.sendify.server;

import com.sendify.server.mcp.ShipmentTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

    @Bean
    public ToolCallbackProvider shipmentTools(ShipmentTool shipmentTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(shipmentTool)
                .build();
    }

}
