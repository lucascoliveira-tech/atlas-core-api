package br.com.lucascoliveira.atlas.core.decision.application;

import java.util.UUID;

public class DiagnosticSessionNotFoundException extends RuntimeException {

    public DiagnosticSessionNotFoundException(UUID id) {
        super("Diagnostic session not found: " + id);
    }
}
