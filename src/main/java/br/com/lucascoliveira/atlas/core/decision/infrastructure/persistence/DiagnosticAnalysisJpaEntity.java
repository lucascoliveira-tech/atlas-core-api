package br.com.lucascoliveira.atlas.core.decision.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisStatus;

@Entity
@Table(name = "diagnostic_session_analysis")
class DiagnosticAnalysisJpaEntity {

    @Id
    UUID id;

    @Column(name = "session_id", nullable = false)
    UUID sessionId;

    @Column(nullable = false)
    int version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    DiagnosticAnalysisStatus status;

    @Column(name = "correlation_id", nullable = false)
    UUID correlationId;

    @Column(name = "request_id", nullable = false)
    UUID requestId;

    @Column(name = "request_key", nullable = false, length = 200)
    String requestKey;

    @Column(name = "title_snapshot", nullable = false, length = 160)
    String titleSnapshot;

    @Column(name = "scenario_snapshot", nullable = false, columnDefinition = "text")
    String scenarioSnapshot;

    @Column(name = "result_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    String resultJson;

    @Column(name = "error_code", length = 80)
    String errorCode;

    @Column(name = "error_detail", columnDefinition = "text")
    String errorDetail;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "completed_at")
    Instant completedAt;
}
