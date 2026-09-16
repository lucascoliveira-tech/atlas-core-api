package br.com.lucascoliveira.atlas.core.intelligence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort.ArchitecturalAnalysisCommand;
import br.com.lucascoliveira.atlas.core.intelligence.ArchitecturalAnalysisPort.ArchitecturalAnalysisResult;

@Component
@ConditionalOnProperty(name = "atlas.intelligence.provider", havingValue = "http")
class AtlasIntelligenceHttpAdapter implements ArchitecturalAnalysisPort {

    private final RestClient client;
    private final String bearerToken;
    private final MeterRegistry meterRegistry;

    AtlasIntelligenceHttpAdapter(
        RestClient intelligenceRestClient,
        @org.springframework.beans.factory.annotation.Value("${atlas.intelligence.auth.bearer-token:}") String bearerToken,
        MeterRegistry meterRegistry
    ) {
        this.client = intelligenceRestClient;
        this.bearerToken = bearerToken;
        this.meterRegistry = meterRegistry;
        if (bearerToken.isBlank()) {
            throw new IllegalStateException("atlas.intelligence.auth.bearer-token is required for HTTP provider");
        }
    }

    @Override
    public ArchitecturalAnalysisResult generate(ArchitecturalAnalysisCommand command) {
        AtlasIntelligenceRequest request = AtlasIntelligenceRequest.from(command);
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            AtlasIntelligenceResponse response = client.post()
                .uri("/api/v1/architectural-analyses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, command))
                .body(request)
                .retrieve()
                .body(AtlasIntelligenceResponse.class);
            if (response == null || response.result() == null) {
                throw invalidResponse("Intelligence service returned an empty result", null);
            }
            meterRegistry.counter("atlas.intelligence.requests", "outcome", "success").increment();
            return response.result();
        } catch (ResourceAccessException exception) {
            meterRegistry.counter("atlas.intelligence.requests", "outcome", "transport_error").increment();
            throw new IntelligenceProviderException(
                isTimeout(exception)
                    ? IntelligenceProviderException.Category.TIMEOUT
                    : IntelligenceProviderException.Category.UNAVAILABLE,
                0,
                "Intelligence service is unavailable",
                exception
            );
        } catch (org.springframework.web.client.HttpStatusCodeException exception) {
            meterRegistry.counter("atlas.intelligence.requests", "outcome", "http_error").increment();
            throw mapHttpError(exception);
        } catch (org.springframework.web.client.RestClientException exception) {
            meterRegistry.counter("atlas.intelligence.requests", "outcome", "invalid_response").increment();
            throw invalidResponse("Intelligence service returned an invalid response", exception);
        } finally {
            sample.stop(Timer.builder("atlas.intelligence.duration").register(meterRegistry));
        }
    }

    private void addHeaders(HttpHeaders headers, ArchitecturalAnalysisCommand command) {
        headers.set("X-Correlation-Id", command.correlationId().toString());
        headers.set("Idempotency-Key", command.idempotencyKey());
        if (!bearerToken.isBlank()) {
            headers.setBearerAuth(bearerToken);
        }
    }

    private IntelligenceProviderException mapHttpError(
        org.springframework.web.client.HttpStatusCodeException exception
    ) {
        int status = exception.getStatusCode().value();
        IntelligenceProviderException.Category category = switch (status) {
            case 400, 422 -> IntelligenceProviderException.Category.REJECTED;
            case 401 -> IntelligenceProviderException.Category.UNAUTHENTICATED;
            case 403 -> IntelligenceProviderException.Category.FORBIDDEN;
            case 409 -> IntelligenceProviderException.Category.CONFLICT;
            case 429 -> IntelligenceProviderException.Category.RATE_LIMITED;
            default -> status >= 500
                ? IntelligenceProviderException.Category.UNAVAILABLE
                : IntelligenceProviderException.Category.INVALID_RESPONSE;
        };
        return new IntelligenceProviderException(
            category,
            status,
            "Intelligence service request failed with status " + status,
            exception
        );
    }

    private IntelligenceProviderException invalidResponse(String message, Throwable cause) {
        return new IntelligenceProviderException(
            IntelligenceProviderException.Category.INVALID_RESPONSE,
            0,
            message,
            cause
        );
    }

    private boolean isTimeout(ResourceAccessException exception) {
        return exception.getCause() instanceof java.net.http.HttpTimeoutException
            || exception.getMessage() != null && exception.getMessage().toLowerCase().contains("timeout");
    }

    record AtlasIntelligenceRequest(
        String contractVersion,
        java.util.UUID requestId,
        java.util.UUID sessionId,
        String analysisVersion,
        String title,
        String scenario
    ) {
        static AtlasIntelligenceRequest from(ArchitecturalAnalysisCommand command) {
            return new AtlasIntelligenceRequest(
                "1.0",
                command.requestId(),
                command.sessionId(),
                Integer.toString(command.analysisVersion()),
                command.title(),
                command.scenario()
            );
        }
    }

    record AtlasIntelligenceResponse(
        String contractVersion,
        java.util.UUID requestId,
        java.util.UUID correlationId,
        String status,
        ArchitecturalAnalysisResult result
    ) {
    }
}
