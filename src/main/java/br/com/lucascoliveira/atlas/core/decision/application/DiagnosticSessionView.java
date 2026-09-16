package br.com.lucascoliveira.atlas.core.decision.application;

import java.time.Instant;
import java.util.UUID;

import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSession;
import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSessionStatus;

public record DiagnosticSessionView(
    UUID id,
    String title,
    String scenario,
    DiagnosticSessionStatus status,
    Instant createdAt,
    Instant updatedAt
) {

    public static DiagnosticSessionView from(DiagnosticSession session) {
        return new DiagnosticSessionView(
            session.id(),
            session.title(),
            session.scenario(),
            session.status(),
            session.createdAt(),
            session.updatedAt()
        );
    }
}
