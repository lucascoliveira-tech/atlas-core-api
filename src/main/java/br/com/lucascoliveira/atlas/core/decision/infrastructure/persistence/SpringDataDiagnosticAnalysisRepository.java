package br.com.lucascoliveira.atlas.core.decision.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataDiagnosticAnalysisRepository extends JpaRepository<DiagnosticAnalysisJpaEntity, UUID> {

    Optional<DiagnosticAnalysisJpaEntity> findBySessionIdAndRequestKey(UUID sessionId, String requestKey);

    Optional<DiagnosticAnalysisJpaEntity> findFirstBySessionIdOrderByVersionDesc(UUID sessionId);

    Optional<DiagnosticAnalysisJpaEntity> findBySessionIdAndVersion(UUID sessionId, int version);

    boolean existsBySessionIdAndVersion(UUID sessionId, int version);
}
