package br.com.lucascoliveira.atlas.core.decision.application;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;

import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSession;

@Service
public class CreateDiagnosticSessionUseCase {

    private final DiagnosticSessionPersistencePort persistence;
    private final Clock clock;

    public CreateDiagnosticSessionUseCase(DiagnosticSessionPersistencePort persistence, Clock clock) {
        this.persistence = persistence;
        this.clock = clock;
    }

    public DiagnosticSessionView execute(String title, String scenario) {
        return DiagnosticSessionView.from(
            persistence.save(DiagnosticSession.create(title, scenario, Instant.now(clock)))
        );
    }
}
