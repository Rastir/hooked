package com.flaco.hooked.configuration;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {

    public static Pageable createPageable(int page, int size, String sortBy, Sort.Direction direction) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(Math.max(1, size), 100); // Max 100
        return PageRequest.of(validPage, validSize, Sort.by(direction, sortBy));
    }

    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, "fechaCreacion", Sort.Direction.DESC);
    }
}