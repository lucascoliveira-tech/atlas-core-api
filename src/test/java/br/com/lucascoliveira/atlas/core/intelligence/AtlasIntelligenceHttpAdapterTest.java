package br.com.lucascoliveira.atlas.core.intelligence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.UUID;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class AtlasIntelligenceHttpAdapterTest {

    private HttpServer server;
    private AtlasIntelligenceHttpAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        adapter = new AtlasIntelligenceHttpAdapter(
            RestClient.builder().baseUrl("http://localhost:" + server.getAddress().getPort()).build(),
            "secret-token",
            new SimpleMeterRegistry()
        );
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsVersionedRequestAndPropagatesHeaders() {
        server.createContext("/api/v1/architectural-analyses", exchange -> {
            assertThat(exchange.getRequestMethod()).isEqualTo("POST");
            assertThat(exchange.getRequestHeaders().getFirst("X-Correlation-Id")).isNotBlank();
            assertThat(exchange.getRequestHeaders().getFirst("Idempotency-Key")).isEqualTo("request-1");
            assertThat(exchange.getRequestHeaders().getFirst("Authorization")).isEqualTo("Bearer secret-token");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] response = """
                {
                  "contractVersion":"1.0",
                  "status":"COMPLETED",
                  "result":{
                    "schemaVersion":"1.0",
                    "summary":"summary",
                    "suggestedComponents":[],
                    "recommendedTechnologies":[],
                    "risks":[],
                    "resilienceRecommendations":[],
                    "observabilityRecommendations":[],
                    "decisions":[],
                    "intelligenceGuidance":{
                      "whenAiHelps":"ai",
                      "whenRagHelps":"rag",
                      "whenMcpHelps":"mcp",
                      "whenNotNeeded":"not needed"
                    }
                  }
                }
                """.getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(response);
            }
        });

        ArchitecturalAnalysisPort.ArchitecturalAnalysisResult result = adapter.generate(command());

        assertThat(result.summary()).isEqualTo("summary");
    }

    @Test
    void mapsUnavailableResponse() {
        server.createContext("/api/v1/architectural-analyses", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });

        assertThatThrownBy(() -> adapter.generate(command()))
            .isInstanceOf(IntelligenceProviderException.class)
            .satisfies(error -> assertThat(((IntelligenceProviderException) error).category())
                .isEqualTo(IntelligenceProviderException.Category.UNAVAILABLE));
    }

    private ArchitecturalAnalysisPort.ArchitecturalAnalysisCommand command() {
        return new ArchitecturalAnalysisPort.ArchitecturalAnalysisCommand(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "request-1",
            UUID.randomUUID(),
            1,
            "Title",
            "Scenario",
            Instant.now().plusSeconds(10)
        );
    }
}
