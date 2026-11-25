package com.maquigest.platform.profiles.domain.model.queries;
public record GetCompanyProfileByIdQuery(Long id) {
    public GetCompanyProfileByIdQuery {
        if (id == null || id <= 0) throw new IllegalArgumentException("id must be positive");
    }
}
