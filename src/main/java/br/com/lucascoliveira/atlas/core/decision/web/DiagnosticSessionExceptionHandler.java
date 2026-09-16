package br.com.lucascoliveira.atlas.core.decision.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticSessionNotFoundException;
import br.com.lucascoliveira.atlas.core.decision.application.InvalidPaginationException;
import br.com.lucascoliveira.atlas.core.decision.application.ArchitecturalAnalysisProviderException;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisConflictException;
import br.com.lucascoliveira.atlas.core.decision.application.DiagnosticAnalysisNotFoundException;
import br.com.lucascoliveira.atlas.core.decision.application.InvalidCorrelationIdException;
import br.com.lucascoliveira.atlas.core.intelligence.IntelligenceProviderException;

@RestControllerAdvice
class DiagnosticSessionExceptionHandler {

    @ExceptionHandler(DiagnosticSessionNotFoundException.class)
    ProblemDetail handleNotFound(
        DiagnosticSessionNotFoundException exception,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        return problem(HttpStatus.NOT_FOUND, "Diagnostic session not found", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidPaginationException.class)
    ProblemDetail handleInvalidPagination(
        InvalidPaginationException exception,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid pagination", exception.getMessage(), request);
    }

    @ExceptionHandler(DiagnosticAnalysisNotFoundException.class)
    ProblemDetail handleAnalysisNotFound(
        DiagnosticAnalysisNotFoundException exception,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        return problem(HttpStatus.NOT_FOUND, "Architectural analysis not found", exception.getMessage(), request);
    }

    @ExceptionHandler(DiagnosticAnalysisConflictException.class)
    ProblemDetail handleAnalysisConflict(
        DiagnosticAnalysisConflictException exception,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        return problem(HttpStatus.CONFLICT, "Analysis request conflict", exception.getMessage(), request);
    }

    @ExceptionHandler(ArchitecturalAnalysisProviderException.class)
    ProblemDetail handleProviderFailure(
        ArchitecturalAnalysisProviderException exception,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        if (exception.getCause() instanceof IntelligenceProviderException providerException) {
            status = switch (providerException.category()) {
                case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
                case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
                case UNAVAILABLE -> HttpStatus.BAD_GATEWAY;
                default -> HttpStatus.BAD_GATEWAY;
            };
        }
        return problem(status, "Architectural analysis provider failure", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidCorrelationIdException.class)
    ProblemDetail handleInvalidCorrelation(
        InvalidCorrelationIdException exception,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid correlation ID", exception.getMessage(), request);
    }

    private ProblemDetail problem(
        HttpStatus status,
        String title,
        String detail,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://atlas.example/problems/diagnostic-session"));
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId != null && !correlationId.isBlank()) {
            problem.setProperty("correlationId", correlationId);
        }
        return problem;
    }
}
