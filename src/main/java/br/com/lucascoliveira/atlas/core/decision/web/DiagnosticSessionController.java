package br.com.lucascoliveira.atlas.core.decision.web;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.lucascoliveira.atlas.core.decision.application.CreateDiagnosticSessionUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticSessionView;
import br.com.lucascoliveira.atlas.core.decision.application.GetDiagnosticSessionUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.ListDiagnosticSessionsUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.GenerateDiagnosticAnalysisUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.GetDiagnosticAnalysisUseCase;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisView;
import br.com.lucascoliveira.atlas.core.decision.application.InvalidCorrelationIdException;

@RestController
@Validated
@RequestMapping("/api/v1/diagnostic-sessions")
class DiagnosticSessionController {

    private final CreateDiagnosticSessionUseCase create;
    private final GetDiagnosticSessionUseCase get;
    private final ListDiagnosticSessionsUseCase list;
    private final GenerateDiagnosticAnalysisUseCase generateAnalysis;
    private final GetDiagnosticAnalysisUseCase getAnalysis;

    DiagnosticSessionController(
        CreateDiagnosticSessionUseCase create,
        GetDiagnosticSessionUseCase get,
        ListDiagnosticSessionsUseCase list,
        GenerateDiagnosticAnalysisUseCase generateAnalysis,
        GetDiagnosticAnalysisUseCase getAnalysis
    ) {
        this.create = create;
        this.get = get;
        this.list = list;
        this.generateAnalysis = generateAnalysis;
        this.getAnalysis = getAnalysis;
    }

    @PostMapping("/{id}/analysis")
    @Operation(summary = "Generate an architectural analysis")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Architectural analysis generated"),
        @ApiResponse(responseCode = "400", description = "Invalid correlation ID"),
        @ApiResponse(responseCode = "404", description = "Diagnostic session not found"),
        @ApiResponse(responseCode = "409", description = "Analysis request conflict"),
        @ApiResponse(responseCode = "502", description = "Intelligence provider failure")
    })
    ResponseEntity<DiagnosticAnalysisResponse> generateAnalysis(
        @PathVariable UUID id,
        @Parameter(description = "Stable key used to safely retry the same analysis request", required = true)
        @RequestHeader("Idempotency-Key") @Size(max = 200) @NotBlank String requestKey,
        @Parameter(description = "Functional correlation identifier")
        @RequestHeader(value = "X-Correlation-Id", required = false) String correlationHeader
    ) {
        UUID correlationId;
        try {
            correlationId = correlationHeader == null ? UUID.randomUUID() : UUID.fromString(correlationHeader);
        } catch (IllegalArgumentException exception) {
            throw new InvalidCorrelationIdException(correlationHeader);
        }
        DiagnosticAnalysisResponse response = DiagnosticAnalysisResponse.from(
            generateAnalysis.execute(id, requestKey, correlationId)
        );
        return ResponseEntity.status(response.status().equals("COMPLETED") ? 201 : 200).body(response);
    }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "Get the latest architectural analysis")
    DiagnosticAnalysisResponse latestAnalysis(@PathVariable UUID id) {
        return DiagnosticAnalysisResponse.from(getAnalysis.latest(id));
    }

    @GetMapping("/{id}/analysis/{version}")
    @Operation(summary = "Get a specific architectural analysis version")
    DiagnosticAnalysisResponse analysisVersion(@PathVariable UUID id, @PathVariable @Min(1) int version) {
        return DiagnosticAnalysisResponse.from(getAnalysis.version(id, version));
    }

    @PostMapping
    @Operation(summary = "Create a diagnostic session")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Diagnostic session created"),
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(
            mediaType = "application/problem+json",
            examples = @ExampleObject(value = "{\"status\":400,\"title\":\"Bad Request\"}")
        ))
    })
    ResponseEntity<DiagnosticSessionResponse> create(@Valid @RequestBody CreateDiagnosticSessionRequest request) {
        DiagnosticSessionResponse response = DiagnosticSessionResponse.from(
            create.execute(request.title(), request.scenario())
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a diagnostic session by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Diagnostic session found"),
        @ApiResponse(responseCode = "400", description = "Invalid UUID"),
        @ApiResponse(responseCode = "404", description = "Diagnostic session not found")
    })
    DiagnosticSessionResponse get(@PathVariable UUID id) {
        return DiagnosticSessionResponse.from(get.execute(id));
    }

    @GetMapping
    @Operation(summary = "List diagnostic sessions")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Diagnostic sessions listed"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination")
    })
    DiagnosticSessionPageResponse list(
        @Parameter(description = "Zero-based page number", schema = @Schema(minimum = "0", defaultValue = "0"))
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size, from 1 to 100", schema = @Schema(minimum = "1", maximum = "100", defaultValue = "20"))
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return DiagnosticSessionPageResponse.from(list.execute(page, size));
    }

    @Schema(description = "Data required to create a diagnostic session")
    record CreateDiagnosticSessionRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 10_000) String scenario
    ) {
    }

    @Schema(description = "Diagnostic session")
    record DiagnosticSessionResponse(
        UUID id,
        String title,
        String scenario,
        br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSessionStatus status,
        java.time.Instant createdAt,
        java.time.Instant updatedAt
    ) {
        static DiagnosticSessionResponse from(DiagnosticSessionView view) {
            return new DiagnosticSessionResponse(
                view.id(),
                view.title(),
                view.scenario(),
                view.status(),
                view.createdAt(),
                view.updatedAt()
            );
        }
    }

    @Schema(description = "Paginated diagnostic session result")
    record DiagnosticSessionPageResponse(
        java.util.List<DiagnosticSessionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {
        static DiagnosticSessionPageResponse from(Page<DiagnosticSessionView> result) {
            return new DiagnosticSessionPageResponse(
                result.getContent().stream().map(DiagnosticSessionResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
            );
        }
    }

    record DiagnosticAnalysisResponse(
        UUID analysisId,
        UUID sessionId,
        int version,
        String status,
        UUID correlationId,
        br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort.ArchitecturalAnalysisResult result,
        String errorCode,
        String errorDetail,
        java.time.Instant createdAt,
        java.time.Instant completedAt
    ) {
        static DiagnosticAnalysisResponse from(DiagnosticAnalysisView view) {
            return new DiagnosticAnalysisResponse(
                view.analysisId(),
                view.sessionId(),
                view.version(),
                view.status().name(),
                view.correlationId(),
                view.result(),
                view.errorCode(),
                view.errorDetail(),
                view.createdAt(),
                view.completedAt()
            );
        }
    }
}
