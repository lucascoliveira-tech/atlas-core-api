package br.com.lucascoliveira.atlas.core.intelligence;

public class IntelligenceProviderException extends RuntimeException {

    private final Category category;
    private final int statusCode;

    public IntelligenceProviderException(Category category, int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.category = category;
        this.statusCode = statusCode;
    }

    public Category category() {
        return category;
    }

    public int statusCode() {
        return statusCode;
    }

    public enum Category {
        REJECTED,
        UNAUTHENTICATED,
        FORBIDDEN,
        CONFLICT,
        RATE_LIMITED,
        UNAVAILABLE,
        TIMEOUT,
        INVALID_RESPONSE
    }
}
