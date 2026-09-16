package br.com.lucascoliveira.atlas.core.decision.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSession;
import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSessionStatus;
import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort;
import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort.ArchitecturalAnalysisCommand;
import br.com.lucascoliveira.atlas.core.intelligence.IntelligenceProviderException;

@Service
public class GenerateDiagnosticAnalysisUseCase {

    private final DiagnosticSessionPersistencePort sessionPersistence;
    private final DiagnosticAnalysisPersistencePort analysisPersistence;
    private final ArchitecturalAnalysisPort analysisPort;
    private final Clock clock;
    private final Duration timeout;

    public GenerateDiagnosticAnalysisUseCase(
        DiagnosticSessionPersistencePort sessionPersistence,
        DiagnosticAnalysisPersistencePort analysisPersistence,
        ArchitecturalAnalysisPort analysisPort,
        Clock clock,
        @Value("${atlas.intelligence.timeout:10s}") Duration timeout
    ) {
        this.sessionPersistence = sessionPersistence;
        this.analysisPersistence = analysisPersistence;
        this.analysisPort = analysisPort;
        this.clock = clock;
        this.timeout = timeout;
    }

    public DiagnosticAnalysisView execute(UUID sessionId, String requestKey, UUID correlationId) {
        DiagnosticAnalysisPersistencePort.StoredAnalysis existing =
            analysisPersistence.findByRequestKey(sessionId, requestKey).orElse(null);
        if (existing != null) {
            if (existing.status() == DiagnosticAnalysisStatus.COMPLETED && existing.result() != null) {
                return existing.result();
            }
            if (existing.status() == DiagnosticAnalysisStatus.FAILED) {
                throw new DiagnosticAnalysisConflictException(
                    "An analysis with this idempotency key has already failed"
                );
            }
            throw new DiagnosticAnalysisConflictException(
                "An analysis with this idempotency key is already being processed"
            );
        }

        DiagnosticSession session = sessionPersistence.findById(sessionId)
            .orElseThrow(() -> new DiagnosticSessionNotFoundException(sessionId));
        if (session.status() != DiagnosticSessionStatus.DRAFT
            && session.status() != DiagnosticSessionStatus.COMPLETED
            && session.status() != DiagnosticSessionStatus.FAILED) {
            throw new DiagnosticAnalysisConflictException(
                "Diagnostic session must be in DRAFT state to start an analysis"
            );
        }

        Instant now = Instant.now(clock);
        UUID requestId = UUID.randomUUID();
        int version = analysisPersistence.nextVersion(sessionId);
        ArchitecturalAnalysisCommand command = new ArchitecturalAnalysisCommand(
            session.id(),
            correlationId,
            requestKey,
            requestId,
            version,
            session.title(),
            session.scenario(),
            now.plus(timeout)
        );
        DiagnosticAnalysisPersistencePort.StoredAnalysis processing =
            analysisPersistence.createProcessing(sessionId, version, correlationId, requestKey, command);
        sessionPersistence.save(session.transitionTo(DiagnosticSessionStatus.ANALYZING, now));

        try {
            ArchitecturalAnalysisPort.ArchitecturalAnalysisResult result = analysisPort.generate(command);
            DiagnosticAnalysisView completed = new DiagnosticAnalysisView(
                processing.analysisId(),
                sessionId,
                version,
                DiagnosticAnalysisStatus.COMPLETED,
                correlationId,
                result,
                null,
                null,
                processing.createdAt(),
                Instant.now(clock)
            );
            DiagnosticAnalysisPersistencePort.StoredAnalysis saved = analysisPersistence.complete(
                processing.analysisId(),
                completed
            );
            sessionPersistence.save(
                session.transitionTo(DiagnosticSessionStatus.ANALYZING, now)
                    .transitionTo(DiagnosticSessionStatus.COMPLETED, Instant.now(clock))
            );
            return saved.result();
        } catch (RuntimeException exception) {
            if (exception instanceof IntelligenceProviderException providerException
                && providerException.category() == IntelligenceProviderException.Category.CONFLICT) {
                analysisPersistence.fail(
                    processing.analysisId(),
                    "ANALYSIS_PROVIDER_CONFLICT",
                    providerException.getMessage()
                );
                sessionPersistence.save(
                    session.transitionTo(DiagnosticSessionStatus.ANALYZING, now)
                        .transitionTo(DiagnosticSessionStatus.FAILED, Instant.now(clock))
                );
                throw new DiagnosticAnalysisConflictException(
                    "The intelligence service already processed this idempotency key"
                );
            }
            analysisPersistence.fail(processing.analysisId(), "ANALYSIS_PROVIDER_FAILURE", exception.getMessage());
            sessionPersistence.save(
                session.transitionTo(DiagnosticSessionStatus.ANALYZING, now)
                    .transitionTo(DiagnosticSessionStatus.FAILED, Instant.now(clock))
            );
            throw new ArchitecturalAnalysisProviderException(
                "Architectural analysis provider failed",
                exception
            );
        }
    }
}
