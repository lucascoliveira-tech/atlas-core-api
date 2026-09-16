package br.com.lucascoliveira.atlas.core.decision.application;

import java.util.Optional;
import java.util.UUID;

import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort.ArchitecturalAnalysisCommand;

public interface DiagnosticAnalysisPersistencePort {

    Optional<StoredAnalysis> findByRequestKey(UUID sessionId, String requestKey);

    Optional<StoredAnalysis> findLatest(UUID sessionId);

    Optional<StoredAnalysis> findByVersion(UUID sessionId, int version);

    int nextVersion(UUID sessionId);

    StoredAnalysis createProcessing(
        UUID sessionId,
        int version,
        UUID correlationId,
        String requestKey,
        ArchitecturalAnalysisCommand command
    );

    StoredAnalysis complete(UUID analysisId, DiagnosticAnalysisView result);

    StoredAnalysis fail(UUID analysisId, String errorCode, String errorDetail);

    record StoredAnalysis(
        UUID analysisId,
        UUID sessionId,
        int version,
        DiagnosticAnalysisStatus status,
        UUID correlationId,
        ArchitecturalAnalysisCommand command,
        DiagnosticAnalysisView result,
        String requestKey,
        java.time.Instant createdAt,
        java.time.Instant completedAt
    ) {
    }
}
