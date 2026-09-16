package br.com.lucascoliveira.atlas.core.decision.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class DiagnosticSessionTest {

    private static final Instant NOW = Instant.parse("2026-09-16T20:00:00Z");

    @Test
    void createsDraftSessionWithGeneratedIdAndTimestamps() {
        DiagnosticSession session = DiagnosticSession.create("  Latency  ", "  Slow checkout  ", NOW);

        assertThat(session.id()).isNotNull();
        assertThat(session.title()).isEqualTo("Latency");
        assertThat(session.scenario()).isEqualTo("Slow checkout");
        assertThat(session.status()).isEqualTo(DiagnosticSessionStatus.DRAFT);
        assertThat(session.createdAt()).isEqualTo(NOW);
        assertThat(session.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsBlankTitle() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> DiagnosticSession.create(" ", "A scenario", NOW));
    }

    @Test
    void rejectsScenarioLongerThanTenThousandCharacters() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> DiagnosticSession.create("Title", "x".repeat(10_001), NOW));
    }
}
