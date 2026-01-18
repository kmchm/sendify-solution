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

#### 1. Tracking Request Initiation
- The process starts with `trackShipment(String referenceNumber)`.
- If the reference number is missing or empty, a `TrackingReferenceMissingException` is thrown.

#### 2. STT Number Query
- Calls `sttNumberQuery(trackingNumber, 0, null)` to get the internal shipment ID (`sttId`).
- Builds HTTP headers (optionally with a captcha solution).
- Sends a GET request to the DB Schenker tracking API with the reference number.
- Parses the response to extract the `sttId`.

#### 3. Handling Captcha Challenges
- If the API responds with HTTP 429 (Too Many Requests), it likely requires a captcha solution.
- The `handleCaptchaError` method:
  - Checks if the maximum retry count is reached.
  - Extracts the `captcha-puzzle` header from the response.
  - Uses `DbSchenkerCaptchaSolver.generateCaptcha()` to solve the captcha.
  - Retries the request with the captcha solution.

#### 4. Shipment Query
- Calls `shipmentQuery(sttId, 0, null)` to get detailed shipment information.
- Builds HTTP headers (optionally with a captcha solution).
- Sends a GET request to the API for shipment details.
- Parses the response into a `LandSttResponse` object.
- Logs package and event details.
- Maps the response to an internal DTO (`ShipmentDetailsDto`) using `LandSttResponseMapper`.

#### 5. Captcha Solving Logic

#### `DbSchenkerCaptchaSolver.generateCaptcha(String captchaPuzzleBase64)`:
- Decodes the base64-encoded captcha puzzle.
- Splits it into JWT tokens.
- For each JWT:
  - Decodes the payload to extract the puzzle.
  - Solves the puzzle using `solvePuzzle(byte[] puzzleArray)`.
  - Collects the JWT and its solution.
- Encodes the solutions as a base64 JSON string for the API.

#### `solvePuzzle(byte[] puzzleArray)`:
- Extracts difficulty parameters from the puzzle.
- Iterates over possible nonce values to find one that, when hashed with the puzzle, meets the difficulty requirement.
- Returns the base64-encoded solution.

#### 6. Error Handling
- If any step fails (other than captcha), a `ShipmentTrackingException` is thrown.
- If captcha cannot be solved or the puzzle is missing, a `CaptchaRequiredException` is thrown.

### config package
Contains configuration files.
### controller package
Contains `ShipmentController` that is a simple REST controller for DbSchenkerClient.
### dto package
Contains `external` and `internal` packages. The `external` package is for external DTOs, `internal` — for internal DTOs.
### exception package
Contains `RestExceptionHandler` which is a global exception hadnler for the REST API. Also contains custom runtime exceptions like `CaptchaRequiredException`, `ShipmentTrackingException`, or `Tracking ReferenceMissingException`.
### mapper package
Contains the `LandSttResponseMapper` to map from and to the DTO.
### mcp package
Contains the MCP server API.
### util package
Contains `DbSchenkerCaptchaSolver` that allows to bypass DBSchenker bot protection.
### tests package
Contain tests for the `DbSchenkerClient`.

## Possible Improvements
If I had more time, I would build a local MCP connector in Go that connects a local LLM agent (such as Claude) with the Java Spring service. This approach would make it easier to add features like authentication between the LLM agent and the Java service. Using Go would keep the local process lightweight and efficient. The connector would be generated from the Java Spring OpenAPI specification, allowing the same backend to expose a REST API for human users and an MCP interface for LLMs.

## Code Quality and Modularity
The codebase is designed to be modular and follows established best practices for maintainability and scalability:

### Separation of Concerns:
The application logic is divided into clear packages such as controller, client, dto, exception, mapper, util, and mcp. Each package has a distinct responsibility, making the code easier to understand and modify.

### Dependency Injection:
Spring Boot's dependency injection is used throughout the project, promoting loose coupling and easier testing.

### Configuration Profiles:
Multiple configuration files (application.properties, application-sse.properties, application-stdio.properties) allow for flexible environment setups and protocol switching.

### DTO Usage:
Data transfer objects are used to encapsulate and validate data exchanged between layers, improving code clarity and reducing errors.

### Exception Handling:
A global exception handler ensures consistent error responses and simplifies debugging.

### Testing:
Unit tests are provided to verify core functionality and ensure reliability.

### Extensibility:
By centralizing protocol logic in classes like ShipmentTool and external integrations in DbSchenkerClient, the solution can be easily extended to support new protocols or data sources.

This modular structure not only makes the codebase easier to maintain and test, but also facilitates future enhancements and team collaboration.

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
