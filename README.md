# DB Schenker Package Scraper

This application supports both **SSE** and **STDIO** connections for MCP server integration.

---

## Solution Overview

This project implements an MCP server that can communicate via both SSE (Server-Sent Events) and STDIO protocols. The solution is built using Java (Spring Boot) and is designed to be easily deployable using Docker. It also supports Playwright for browser automation and testing.

## Requirements

-   **[Java 21 (Oracle JDK)](https://www.oracle.com/java/technologies/downloads/#jdk21-mac)**
-   **[Maven](https://maven.apache.org/download.cgi)** (for local builds)
-   **[Docker](https://docs.docker.com/get-docker/)** and **[Docker Compose](https://docs.docker.com/compose/install/)** (for containerized runs)
-   **[Claude client](https://claude.com/download)** (for STDIO version)

---

## SSE Version

### Running with Docker Compose

To build and run the MCP server using Docker Compose:

```bash
docker compose up
```

The server will be available at [http://localhost:8081](http://localhost:8081).

**Note:**
The first build may take a few minutes as it installs all dependencies and browsers.

---

### Testing with MCP Inspector

You can use [MCP Inspector](https://inspector.modelcontextprotocol.io/) to interactively test and inspect your MCP server.

#### How to use MCP Inspector

1. Start your MCP server (see instructions above).
2. Open [MCP Inspector](https://inspector.modelcontextprotocol.io/) in your browser.
3. Enter your server's endpoint (e.g., `http://localhost:8081`) in the Inspector.
4. Use the Inspector to send requests and view structured responses from your MCP server.

![MCP Inspector Screenshot](/media/mcp-inspector.png)

---

## STDIO Version

1. Go to the project root and run `mvn clean package` to compile the project. The JAR file will be in `server/target`.
2. Download a Claude client from [claude.com/download](https://claude.com/download).
3. Open Claude > Settings > Developer > Edit Config, paste the code below and change the path to the location of your JAR file:

    ```json
    {
        "mcpServers": {
            "db-schenker-tracker": {
                "command": "java",
                "args": [
                    "-Dspring.profiles.active=stdio",
                    "-jar",
                    "/Users/kamil/Developer/Sendify-Code-Challenge-2026/server/target/server-0.0.1-SNAPSHOT.jar"
                ]
            }
        }
    }
    ```

4. Restart Claude.

![Claude Example](/media/claude-example.png)

## About `DbSchenkerClient`

The `DbSchenkerClient` class is responsible for fetching and parsing shipment tracking information from the DB Schenker website. Instead of using traditional HTTP clients or browserless scraping libraries, this solution uses **Playwright** to automate a real browser session. This approach is necessary because the DB Schenker website employs bot protection mechanisms that block standard HTTP requests and headless scraping tools.

With Playwright, the client navigates to the tracking page and listens for specific JSON responses sent to the browser. These responses contain the shipment data, which is then parsed and mapped to internal DTOs for further processing. This method ensures reliable data extraction despite anti-bot measures on the target site.

If I had more time, I might have found a more efficient solution for bypassing these protections, but I was unsuccessful during this challenge. I am aware that using Playwright for browser automation is not an optimal solution for production environments due to its resource requirements and complexity, but it was necessary here to reliably extract data from a site with strong bot protection.

## About `ShipmentTool`

The `ShipmentTool` class, located in the `mcp` package, encapsulates the core logic for handling MCP protocol operations within the server. It is responsible for processing shipment-related requests.

This tool acts as a bridge between the business logic and the MCP interface, making it easier to maintain and extend protocol-specific functionality. By centralizing MCP operations in a dedicated class, the solution remains modular and easier to test or adapt for future protocol changes.

## About DTOs

The project uses Data Transfer Objects (DTOs) to structure and validate data exchanged between different layers of the application. DTOs help ensure that only relevant and properly formatted data is passed between controllers, services, and external integrations.

- **external/**  
  Contains DTOs for mapping responses from external APIs, such as DB Schenker (`LandSttResponse.java`, `ShipmentResponse.java`, `TripResponse.java`). These classes represent the structure of data received from third-party services.

- **internal/**  
  Contains DTOs for internal use within the application (`ShipmentDetailsDto.java`). These classes are used to encapsulate and transfer data between internal components, ensuring consistency and type safety.

Using DTOs improves code clarity, reduces errors, and makes it easier to adapt the application to changes in external or internal data formats.

## Code Quality and Modularity

The codebase is designed to be modular and follows established best practices for maintainability and scalability:

- **Separation of Concerns:**  
  The application logic is divided into clear packages such as `controller`, `client`, `dto`, `exception`, and `mcp`. Each package has a distinct responsibility, making the code easier to understand and modify.

- **Dependency Injection:**  
  Spring Boot's dependency injection is used throughout the project, promoting loose coupling and easier testing.

- **Configuration Profiles:**  
  Multiple configuration files (`application.properties`, `application-sse.properties`, `application-stdio.properties`) allow for flexible environment setups and protocol switching.

- **DTO Usage:**  
  Data transfer objects are used to encapsulate and validate data exchanged between layers, improving code clarity and reducing errors.

- **Exception Handling:**  
  A global exception handler ensures consistent error responses and simplifies debugging.

- **Testing:**  
  Unit and integration tests are provided to verify core functionality and ensure reliability.

- **Extensibility:**  
  By centralizing protocol logic in classes like `ShipmentTool` and external integrations in `DbSchenkerClient`, the solution can be easily extended to support new protocols or data sources.

This modular structure not only makes the codebase easier to maintain and test, but also facilitates future enhancements and team collaboration.
