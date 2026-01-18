# DB Schenker Package Scraper

This application supports both **SSE** and **STDIO** connections for MCP server integration.

---

## Solution Overview

This project implements an MCP server that can communicate via both SSE (Server-Sent Events) and STDIO protocols.

The solution is built using Java (Spring Boot) and is designed to be easily deployable using Docker.

## Project Structure
The solution follows the typical Java Spring project structure.
### client
Contains `DbSchenkerClient` class that is a client handling the communication with DbSchenker website to intercept requests and communicate with their API.
### config
Contains configuration files.
### controller
Contains `ShipmentController` that is a simple REST controller for DbSchenkerClient.
### dto
Contains `external` and `internal` packages. The `external` package is for external DTOs, `internal` — for internal DTOs.
### exception
Contains `RestExceptionHandler` which is a global exception hadnler for the REST API. Also contains custom runtime exceptions like `CaptchaRequiredException`, `ShipmentTrackingException`, or `Tracking ReferenceMissingException`.
### mapper
Contains the `LandSttResponseMapper` to map from and to the DTO.
### mcp
Contains the MCP server API.
### util
Contains `DbSchenkerCaptchaSolver` that allows to bypass DBSchenker bot protection.
### tests
Contain tests for the `DbSchenkerClient`.

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

---

### Testing with MCP Inspector

You can use [MCP Inspector](https://inspector.modelcontextprotocol.io/) to interactively test and inspect your MCP server.

#### How to use MCP Inspector

1. Start your MCP server (see instructions above).
2. Start the MCP Inspector in your browser.
3. Enter your server's endpoint (e.g., `http://localhost:8081/mcp/sse`) in the Inspector.
4. Use the Inspector to send requests and view structured responses from the MCP server.

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

![Claude Example 2](/media/claude-example-2.png)

## Possible Improvements
If I had more time, I would build a local MCP connector in Go that connects a local LLM agent (such as Claude) with the Java Spring service. This approach would make it easier to add features like authentication between the LLM agent and the Java service. Using Go would keep the local process lightweight and efficient. The connector would be generated from the Java Spring OpenAPI specification, allowing the same backend to expose a REST API for human users and an MCP interface for LLMs.