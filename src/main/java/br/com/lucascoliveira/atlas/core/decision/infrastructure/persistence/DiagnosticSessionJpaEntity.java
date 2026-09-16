package br.com.lucascoliveira.atlas.core.decision.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSession;
import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSessionStatus;

@Entity
@Table(name = "diagnostic_session")
class DiagnosticSessionJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String scenario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DiagnosticSessionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DiagnosticSessionJpaEntity() {
    }

    static DiagnosticSessionJpaEntity fromDomain(DiagnosticSession session) {
        DiagnosticSessionJpaEntity entity = new DiagnosticSessionJpaEntity();
        entity.id = session.id();
        entity.title = session.title();
        entity.scenario = session.scenario();
        entity.status = session.status();
        entity.createdAt = session.createdAt();
        entity.updatedAt = session.updatedAt();
        return entity;
    }

    DiagnosticSession toDomain() {
        return DiagnosticSession.restore(id, title, scenario, status, createdAt, updatedAt);
    }
}
