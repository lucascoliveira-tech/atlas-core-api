package br.com.lucascoliveira.atlas.core.decision.application;

import java.time.Instant;
import java.util.UUID;

import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort.ArchitecturalAnalysisResult;

public record DiagnosticAnalysisView(
    UUID analysisId,
    UUID sessionId,
    int version,
    DiagnosticAnalysisStatus status,
    UUID correlationId,
    ArchitecturalAnalysisResult result,
    String errorCode,
    String errorDetail,
    Instant createdAt,
    Instant completedAt
) {
}
