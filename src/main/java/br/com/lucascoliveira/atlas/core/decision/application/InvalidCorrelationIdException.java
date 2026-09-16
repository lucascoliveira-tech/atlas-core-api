package br.com.lucascoliveira.atlas.core.decision.application;

public class InvalidCorrelationIdException extends RuntimeException {

    public InvalidCorrelationIdException(String value) {
        super("Invalid X-Correlation-Id: " + value);
    }
}
