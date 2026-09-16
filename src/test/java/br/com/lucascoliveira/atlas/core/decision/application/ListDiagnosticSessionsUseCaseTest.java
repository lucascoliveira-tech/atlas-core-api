package br.com.lucascoliveira.atlas.core.decision.application;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class ListDiagnosticSessionsUseCaseTest {

    private final DiagnosticSessionPersistencePort persistence = mock(DiagnosticSessionPersistencePort.class);
    private final ListDiagnosticSessionsUseCase useCase = new ListDiagnosticSessionsUseCase(persistence);

    @Test
    void requestsNewestFirstWithDeterministicIdTieBreaker() {
        when(persistence.findAll(org.mockito.ArgumentMatchers.any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        useCase.execute(2, 25);

        verify(persistence).findAll(argThat(pageable ->
            pageable.getPageNumber() == 2
                && pageable.getPageSize() == 25
                && pageable.getSort().toString().equals("createdAt: DESC,id: DESC")
        ));
    }

    @Test
    void rejectsPageSizeAboveLimit() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> useCase.execute(0, 101))
            .isInstanceOf(InvalidPaginationException.class);
    }
}
