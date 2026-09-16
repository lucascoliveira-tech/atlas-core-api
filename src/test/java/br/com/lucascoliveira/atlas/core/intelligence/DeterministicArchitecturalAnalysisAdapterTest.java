package br.com.lucascoliveira.atlas.core.intelligence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class DeterministicArchitecturalAnalysisAdapterTest {

    @Test
    void returnsAllArchitecturalAnalysisSectionsWithoutExternalServices() {
        ArchitecturalAnalysisPort port = new DeterministicArchitecturalAnalysisAdapter();
        ArchitecturalAnalysisPort.ArchitecturalAnalysisResult result = port.generate(
            new ArchitecturalAnalysisPort.ArchitecturalAnalysisCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "request-1",
                UUID.randomUUID(),
                1,
                "Checkout latency",
                "The checkout is slow",
                Instant.now().plusSeconds(10)
            )
        );

        assertThat(result.schemaVersion()).isEqualTo("1.0");
        assertThat(result.summary()).isNotBlank();
        assertThat(result.suggestedComponents()).isNotEmpty();
        assertThat(result.recommendedTechnologies()).isNotEmpty();
        assertThat(result.risks()).isNotEmpty();
        assertThat(result.resilienceRecommendations()).isNotEmpty();
        assertThat(result.observabilityRecommendations()).isNotEmpty();
        assertThat(result.decisions()).isNotEmpty();
        assertThat(result.intelligenceGuidance().whenNotNeeded()).contains("not necessary");
    }
}
