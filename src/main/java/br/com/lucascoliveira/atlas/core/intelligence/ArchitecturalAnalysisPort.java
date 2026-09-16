package br.com.lucascoliveira.atlas.core.intelligence;

import java.time.Instant;
import java.util.UUID;

public interface ArchitecturalAnalysisPort {

    ArchitecturalAnalysisResult generate(ArchitecturalAnalysisCommand command);

    record ArchitecturalAnalysisCommand(
        UUID sessionId,
        UUID correlationId,
        String idempotencyKey,
        UUID requestId,
        int analysisVersion,
        String title,
        String scenario,
        Instant deadline
    ) {
    }

    record ArchitecturalAnalysisResult(
        String schemaVersion,
        String summary,
        java.util.List<ComponentSuggestion> suggestedComponents,
        java.util.List<TechnologyRecommendation> recommendedTechnologies,
        java.util.List<Risk> risks,
        java.util.List<String> resilienceRecommendations,
        java.util.List<String> observabilityRecommendations,
        java.util.List<DecisionTradeOff> decisions,
        IntelligenceGuidance intelligenceGuidance
    ) {
    }

    record ComponentSuggestion(String name, String responsibility) {
    }

    record TechnologyRecommendation(String category, String technology, String rationale) {
    }

    record Risk(String description, String impact, String mitigation) {
    }

    record DecisionTradeOff(
        String decision,
        String chosenOption,
        java.util.List<String> alternatives,
        String tradeOffs
    ) {
    }

    record IntelligenceGuidance(
        String whenAiHelps,
        String whenRagHelps,
        String whenMcpHelps,
        String whenNotNeeded
    ) {
    }
}
