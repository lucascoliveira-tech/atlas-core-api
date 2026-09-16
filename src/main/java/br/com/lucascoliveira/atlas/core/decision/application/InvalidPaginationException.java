package br.com.lucascoliveira.atlas.core.decision.application;

public class InvalidPaginationException extends RuntimeException {

    public InvalidPaginationException(String message) {
        super(message);
    }
}
