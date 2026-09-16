package br.com.lucascoliveira.atlas.core.decision.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataDiagnosticSessionRepository extends JpaRepository<DiagnosticSessionJpaEntity, UUID> {
}
