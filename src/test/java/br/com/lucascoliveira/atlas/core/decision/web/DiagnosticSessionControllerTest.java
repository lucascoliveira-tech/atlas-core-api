package br.com.lucascoliveira.atlas.core.decision.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.lucascoliveira.atlas.core.config.SecurityConfiguration;
import br.com.lucascoliveira.atlas.core.decision.application.CreateDiagnosticSessionUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticSessionNotFoundException;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticSessionView;
import br.com.lucascoliveira.atlas.core.decision.application.GetDiagnosticSessionUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.ListDiagnosticSessionsUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.GenerateDiagnosticAnalysisUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.GetDiagnosticAnalysisUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisStatus;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisView;
import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort;
import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSessionStatus;

@WebMvcTest(DiagnosticSessionController.class)
@Import({SecurityConfiguration.class, DiagnosticSessionExceptionHandler.class})
class DiagnosticSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateDiagnosticSessionUseCase create;

    @MockitoBean
    private GetDiagnosticSessionUseCase get;

    @MockitoBean
    private ListDiagnosticSessionsUseCase list;

    @MockitoBean
    private GenerateDiagnosticAnalysisUseCase generateAnalysis;

    @MockitoBean
    private GetDiagnosticAnalysisUseCase getAnalysis;

    @Test
    void createsSessionAndReturnsLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(create.execute("Checkout latency", "The checkout is slow"))
            .thenReturn(view(id));

        mockMvc.perform(post("/api/v1/diagnostic-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Checkout latency","scenario":"The checkout is slow"}
                    """))
            .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/diagnostic-sessions/" + id))
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void rejectsInvalidRequestWithProblemDetails() throws Exception {
        mockMvc.perform(post("/api/v1/diagnostic-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"","scenario":""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsNotFoundAsProblemDetails() throws Exception {
        UUID id = UUID.randomUUID();
        when(get.execute(id)).thenThrow(new DiagnosticSessionNotFoundException(id));

        mockMvc.perform(get("/api/v1/diagnostic-sessions/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejectsMalformedUuidAsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/diagnostic-sessions/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listsSessionsWithPageMetadata() throws Exception {
        when(list.execute(0, 20)).thenReturn(new PageImpl<>(List.of(view(UUID.randomUUID())), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/diagnostic-sessions?page=0&size=20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void generatesAnalysisWithCorrelationAndIdempotencyHeaders() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.parse("2026-09-16T20:00:00Z");
        ArchitecturalAnalysisPort.ArchitecturalAnalysisResult result =
            new ArchitecturalAnalysisPort.ArchitecturalAnalysisResult(
                "1.0", "summary", java.util.List.of(), java.util.List.of(), java.util.List.of(),
                java.util.List.of(), java.util.List.of(), java.util.List.of(),
                new ArchitecturalAnalysisPort.IntelligenceGuidance("ai", "rag", "mcp", "not needed")
            );
        when(generateAnalysis.execute(sessionId, "request-1", correlationId))
            .thenReturn(new DiagnosticAnalysisView(
                analysisId, sessionId, 1, DiagnosticAnalysisStatus.COMPLETED,
                correlationId, result, null, null, now, now
            ));

        mockMvc.perform(post("/api/v1/diagnostic-sessions/{id}/analysis", sessionId)
                .header("Idempotency-Key", "request-1")
                .header("X-Correlation-Id", correlationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.analysisId").value(analysisId.toString()))
            .andExpect(jsonPath("$.result.summary").value("summary"));
    }

    private DiagnosticSessionView view(UUID id) {
        Instant now = Instant.parse("2026-09-16T20:00:00Z");
        return new DiagnosticSessionView(
            id,
            "Checkout latency",
            "The checkout is slow",
            DiagnosticSessionStatus.DRAFT,
            now,
            now
        );
    }
}
