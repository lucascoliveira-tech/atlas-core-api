package br.com.lucascoliveira.atlas.core.decision.application;

import java.util.UUID;

public class DiagnosticAnalysisNotFoundException extends RuntimeException {

    public DiagnosticAnalysisNotFoundException(UUID sessionId) {
        super("No architectural analysis found for diagnostic session: " + sessionId);
    }
}
