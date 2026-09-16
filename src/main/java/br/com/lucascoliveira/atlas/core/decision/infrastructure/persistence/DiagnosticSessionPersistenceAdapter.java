package br.com.lucascoliveira.atlas.core.decision.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticSessionPersistencePort;
import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSession;

@Repository
class DiagnosticSessionPersistenceAdapter implements DiagnosticSessionPersistencePort {

    private final SpringDataDiagnosticSessionRepository repository;

    DiagnosticSessionPersistenceAdapter(SpringDataDiagnosticSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public DiagnosticSession save(DiagnosticSession session) {
        return repository.save(DiagnosticSessionJpaEntity.fromDomain(session)).toDomain();
    }

    @Override
    public Optional<DiagnosticSession> findById(UUID id) {
        return repository.findById(id).map(entity -> entity.toDomain());
    }

    @Override
    public Page<DiagnosticSession> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(entity -> entity.toDomain());
    }
}
