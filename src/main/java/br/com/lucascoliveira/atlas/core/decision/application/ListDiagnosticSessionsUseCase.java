package br.com.lucascoliveira.atlas.core.decision.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ListDiagnosticSessionsUseCase {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Sort SORT = Sort.by(
        Sort.Order.desc("createdAt"),
        Sort.Order.desc("id")
    );

    private final DiagnosticSessionPersistencePort persistence;

    public ListDiagnosticSessionsUseCase(DiagnosticSessionPersistencePort persistence) {
        this.persistence = persistence;
    }

    public Page<DiagnosticSessionView> execute(int page, int size) {
        if (page < 0) {
            throw new InvalidPaginationException("page must be greater than or equal to zero");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return persistence.findAll(PageRequest.of(page, size, SORT))
            .map(DiagnosticSessionView::from);
    }
}
