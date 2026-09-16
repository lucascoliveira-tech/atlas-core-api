package br.com.lucascoliveira.atlas.core.intelligence;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "atlas.intelligence.provider",
    havingValue = "simulator",
    matchIfMissing = true
)
class DeterministicArchitecturalAnalysisAdapter implements ArchitecturalAnalysisPort {

    @Override
    public ArchitecturalAnalysisResult generate(ArchitecturalAnalysisCommand command) {
        return new ArchitecturalAnalysisResult(
            "1.0",
            "A modular API should isolate the diagnostic workflow from persistence and external intelligence providers.",
            List.of(
                new ComponentSuggestion("Core API", "Owns the diagnostic session lifecycle and analysis orchestration."),
                new ComponentSuggestion("Analysis Provider", "Generates architectural recommendations behind a stable port.")
            ),
            List.of(
                new TechnologyRecommendation("API", "Spring MVC", "Matches the existing synchronous HTTP stack."),
                new TechnologyRecommendation("Persistence", "PostgreSQL", "Provides durable transactional storage for sessions and analyses."),
                new TechnologyRecommendation("Observability", "OpenTelemetry", "Provides correlation and provider latency visibility.")
            ),
            List.of(
                new Risk(
                    "External analysis providers may be unavailable or slow.",
                    "The user may not receive an analysis within the request timeout.",
                    "Use bounded timeouts, correlation IDs, idempotency keys and explicit failure states."
                )
            ),
            List.of(
                "Keep provider calls behind a port.",
                "Use bounded timeouts and retry only transient failures.",
                "Persist failed attempts without silently falling back."
            ),
            List.of(
                "Propagate correlation IDs to logs and provider requests.",
                "Record provider duration and outcome metrics.",
                "Expose analysis status through the session API."
            ),
            List.of(
                new DecisionTradeOff(
                    "Analysis execution model",
                    "Synchronous",
                    List.of("Asynchronous job", "Message-driven worker"),
                    "Synchronous processing is simpler for the deterministic provider; asynchronous processing is preferable when real provider latency requires it."
                ),
                new DecisionTradeOff(
                    "Intelligence provider",
                    "Deterministic simulator",
                    List.of("Remote AI provider", "RAG pipeline"),
                    "The simulator is predictable and auditable while preserving the port needed for future intelligence services."
                )
            ),
            new IntelligenceGuidance(
                "Use generative AI when the scenario is ambiguous and requires synthesis of multiple architectural alternatives.",
                "Use RAG when internal ADRs, standards or runbooks should ground recommendations.",
                "Use MCP when analysis must inspect external systems or execute controlled tool actions.",
                "AI, RAG and MCP are not necessary for simple, rule-covered scenarios or when deterministic and auditable output is preferred."
            )
        );
    }
}
