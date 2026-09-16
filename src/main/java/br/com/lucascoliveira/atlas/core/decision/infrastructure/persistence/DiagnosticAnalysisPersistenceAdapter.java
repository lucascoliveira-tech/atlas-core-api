package br.com.lucascoliveira.atlas.core.decision.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisPersistencePort;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisStatus;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisView;
import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort.ArchitecturalAnalysisCommand;
import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort.ArchitecturalAnalysisResult;

@Repository
class DiagnosticAnalysisPersistenceAdapter implements DiagnosticAnalysisPersistencePort {

    private final SpringDataDiagnosticAnalysisRepository repository;
    private final ObjectMapper objectMapper;

    DiagnosticAnalysisPersistenceAdapter(
        SpringDataDiagnosticAnalysisRepository repository,
        ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<StoredAnalysis> findByRequestKey(UUID sessionId, String requestKey) {
        return repository.findBySessionIdAndRequestKey(sessionId, requestKey).map(this::toStored);
    }

    @Override
    public Optional<StoredAnalysis> findLatest(UUID sessionId) {
        return repository.findFirstBySessionIdOrderByVersionDesc(sessionId).map(this::toStored);
    }

    @Override
    public Optional<StoredAnalysis> findByVersion(UUID sessionId, int version) {
        return repository.findBySessionIdAndVersion(sessionId, version).map(this::toStored);
    }

    @Override
    public int nextVersion(UUID sessionId) {
        return repository.findFirstBySessionIdOrderByVersionDesc(sessionId)
            .map(entity -> entity.version + 1)
            .orElse(1);
    }

    @Override
    public StoredAnalysis createProcessing(
        UUID sessionId,
        int version,
        UUID correlationId,
        String requestKey,
        ArchitecturalAnalysisCommand command
    ) {
        DiagnosticAnalysisJpaEntity entity = new DiagnosticAnalysisJpaEntity();
        entity.id = UUID.randomUUID();
        entity.sessionId = sessionId;
        entity.version = version;
        entity.status = DiagnosticAnalysisStatus.PROCESSING;
        entity.correlationId = correlationId;
        entity.requestId = command.requestId();
        entity.requestKey = requestKey;
        entity.titleSnapshot = command.title();
        entity.scenarioSnapshot = command.scenario();
        entity.createdAt = Instant.now();
        return toStored(repository.save(entity));
    }

    @Override
    public StoredAnalysis complete(UUID analysisId, DiagnosticAnalysisView result) {
        DiagnosticAnalysisJpaEntity entity = repository.findById(analysisId)
            .orElseThrow(() -> new IllegalStateException("Analysis record not found: " + analysisId));
        entity.status = DiagnosticAnalysisStatus.COMPLETED;
        entity.resultJson = write(result.result());
        entity.completedAt = result.completedAt();
        return toStored(repository.save(entity));
    }

    @Override
    public StoredAnalysis fail(UUID analysisId, String errorCode, String errorDetail) {
        DiagnosticAnalysisJpaEntity entity = repository.findById(analysisId)
            .orElseThrow(() -> new IllegalStateException("Analysis record not found: " + analysisId));
        entity.status = DiagnosticAnalysisStatus.FAILED;
        entity.errorCode = errorCode;
        entity.errorDetail = errorDetail;
        entity.completedAt = Instant.now();
        return toStored(repository.save(entity));
    }

    private StoredAnalysis toStored(DiagnosticAnalysisJpaEntity entity) {
        ArchitecturalAnalysisCommand command = new ArchitecturalAnalysisCommand(
            entity.sessionId,
            entity.correlationId,
            entity.requestKey,
            entity.requestId,
            entity.version,
            entity.titleSnapshot,
            entity.scenarioSnapshot,
            null
        );
        DiagnosticAnalysisView view = new DiagnosticAnalysisView(
            entity.id,
            entity.sessionId,
            entity.version,
            entity.status,
            entity.correlationId,
            read(entity.resultJson),
            entity.errorCode,
            entity.errorDetail,
            entity.createdAt,
            entity.completedAt
        );
        return new StoredAnalysis(
            entity.id,
            entity.sessionId,
            entity.version,
            entity.status,
            entity.correlationId,
            command,
            view,
            entity.requestKey,
            entity.createdAt,
            entity.completedAt
        );
    }

    private String write(ArchitecturalAnalysisResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize architectural analysis", exception);
        }
    }

    private ArchitecturalAnalysisResult read(String value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, ArchitecturalAnalysisResult.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not deserialize architectural analysis", exception);
        }
    }
}
