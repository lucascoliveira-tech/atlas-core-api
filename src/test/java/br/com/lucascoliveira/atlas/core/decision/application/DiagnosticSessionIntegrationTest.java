package br.com.lucascoliveira.atlas.core.decision.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import br.com.lucascoliveira.atlas.core.TestcontainersConfiguration;
import br.com.lucascoliveira.atlas.core.decision.domain.DiagnosticSessionStatus;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DiagnosticSessionIntegrationTest {

    @Autowired
    private CreateDiagnosticSessionUseCase create;

    @Autowired
    private GetDiagnosticSessionUseCase get;

    @Autowired
    private ListDiagnosticSessionsUseCase list;

    @Autowired
    private GenerateDiagnosticAnalysisUseCase generateAnalysis;

    @Autowired
    private GetDiagnosticAnalysisUseCase getAnalysis;

    @Test
    void persistsAndQueriesDiagnosticSession() {
        DiagnosticSessionView created = create.execute(
            "Checkout latency",
            "The checkout exceeds the latency target."
        );

        DiagnosticSessionView found = get.execute(created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.status()).isEqualTo(DiagnosticSessionStatus.DRAFT);
        assertThat(list.execute(0, 20).getContent())
            .extracting(view -> view.id())
            .contains(created.id());
    }

    @Test
    void returnsNotFoundForUnknownSession() {
        UUID id = UUID.randomUUID();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> get.execute(id))
            .isInstanceOf(DiagnosticSessionNotFoundException.class);
    }

    @Test
    void generatesAndPersistsVersionedArchitecturalAnalysis() {
        DiagnosticSessionView session = create.execute("Checkout latency", "The checkout is slow");

        DiagnosticAnalysisView analysis = generateAnalysis.execute(
            session.id(),
            "integration-request-1",
            UUID.randomUUID()
        );

        assertThat(analysis.status()).isEqualTo(DiagnosticAnalysisStatus.COMPLETED);
        assertThat(analysis.version()).isEqualTo(1);
        assertThat(analysis.result().summary()).isNotBlank();
        assertThat(getAnalysis.latest(session.id()).analysisId()).isEqualTo(analysis.analysisId());
    }

    @Test
    void createsASecondVersionWithANewIdempotencyKey() {
        DiagnosticSessionView session = create.execute("Checkout latency", "The checkout is slow");

        generateAnalysis.execute(session.id(), "integration-request-v1", UUID.randomUUID());
        DiagnosticAnalysisView second = generateAnalysis.execute(
            session.id(),
            "integration-request-v2",
            UUID.randomUUID()
        );

        assertThat(second.version()).isEqualTo(2);
        assertThat(getAnalysis.version(session.id(), 1).version()).isEqualTo(1);
    }
}
