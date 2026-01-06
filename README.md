# Sendify Code Challenge 2026

This application supports both **SSE** and **STDIO** connections for MCP server integration.

---

## Requirements

- **[Java 21 (Oracle JDK)](https://www.oracle.com/java/technologies/downloads/#jdk21-mac)**
- **[Maven](https://maven.apache.org/download.cgi)** (for local builds)
- **[Docker](https://docs.docker.com/get-docker/)** and **[Docker Compose](https://docs.docker.com/compose/install/)** (for containerized runs)
- **[Claude client](https://claude.com/download)** (for STDIO version)

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

Feel free to reach out if you have any questions or issues!
