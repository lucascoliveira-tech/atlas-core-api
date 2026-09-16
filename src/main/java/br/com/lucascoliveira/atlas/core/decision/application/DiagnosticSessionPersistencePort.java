package br.com.lucascoliveira.atlas.core.decision.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSession;

public interface DiagnosticSessionPersistencePort {

    DiagnosticSession save(DiagnosticSession session);

    Optional<DiagnosticSession> findById(UUID id);

    Page<DiagnosticSession> findAll(Pageable pageable);
}
