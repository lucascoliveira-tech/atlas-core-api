package br.com.lucascoliveira.atlas.core.decision.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class DiagnosticSession {

    private final UUID id;
    private final String title;
    private final String scenario;
    private final DiagnosticSessionStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private DiagnosticSession(
        UUID id,
        String title,
        String scenario,
        DiagnosticSessionStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = requireText(title, "title", 160);
        this.scenario = requireText(scenario, "scenario", 10_000);
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }
    }

    public static DiagnosticSession create(String title, String scenario, Instant now) {
        return new DiagnosticSession(
            UUID.randomUUID(),
            title,
            scenario,
            DiagnosticSessionStatus.DRAFT,
            now,
            now
        );
    }

    public static DiagnosticSession restore(
        UUID id,
        String title,
        String scenario,
        DiagnosticSessionStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new DiagnosticSession(id, title, scenario, status, createdAt, updatedAt);
    }

    private static String requireText(String value, String field, int maxLength) {
        Objects.requireNonNull(value, field + " must not be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(field + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }

    public UUID id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String scenario() {
        return scenario;
    }

    public DiagnosticSessionStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public DiagnosticSession transitionTo(DiagnosticSessionStatus nextStatus, Instant now) {
        Objects.requireNonNull(nextStatus, "nextStatus must not be null");
        Objects.requireNonNull(now, "now must not be null");
        if (!isValidTransition(status, nextStatus)) {
            throw new IllegalStateException("Invalid diagnostic session transition: " + status + " -> " + nextStatus);
        }
        return new DiagnosticSession(id, title, scenario, nextStatus, createdAt, now);
    }

    private static boolean isValidTransition(
        DiagnosticSessionStatus currentStatus,
        DiagnosticSessionStatus nextStatus
    ) {
        return switch (currentStatus) {
            case DRAFT -> nextStatus == DiagnosticSessionStatus.ANALYZING;
            case ANALYZING -> nextStatus == DiagnosticSessionStatus.COMPLETED
                || nextStatus == DiagnosticSessionStatus.FAILED;
            case COMPLETED, FAILED -> nextStatus == DiagnosticSessionStatus.ANALYZING;
        };
    }
}
