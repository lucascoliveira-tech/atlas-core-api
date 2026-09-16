package br.com.lucascoliveira.atlas.core.decision.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class GetDiagnosticSessionUseCase {

    private final DiagnosticSessionPersistencePort persistence;

    public GetDiagnosticSessionUseCase(DiagnosticSessionPersistencePort persistence) {
        this.persistence = persistence;
    }

    public DiagnosticSessionView execute(UUID id) {
        return persistence.findById(id)
            .map(DiagnosticSessionView::from)
            .orElseThrow(() -> new DiagnosticSessionNotFoundException(id));
    }
}
