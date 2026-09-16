package br.com.lucascoliveira.atlas.core.decision.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class GetDiagnosticAnalysisUseCase {

    private final DiagnosticAnalysisPersistencePort persistence;

    public GetDiagnosticAnalysisUseCase(DiagnosticAnalysisPersistencePort persistence) {
        this.persistence = persistence;
    }

    public DiagnosticAnalysisView latest(UUID sessionId) {
        return persistence.findLatest(sessionId)
            .map(stored -> stored.result())
            .orElseThrow(() -> new DiagnosticAnalysisNotFoundException(sessionId));
    }

    public DiagnosticAnalysisView version(UUID sessionId, int version) {
        return persistence.findByVersion(sessionId, version)
            .map(stored -> stored.result())
            .orElseThrow(() -> new DiagnosticAnalysisNotFoundException(sessionId));
    }
}
