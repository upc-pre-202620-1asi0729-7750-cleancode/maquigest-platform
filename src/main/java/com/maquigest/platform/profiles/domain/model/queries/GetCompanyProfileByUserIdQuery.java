package com.maquigest.platform.profiles.domain.model.queries;
public record GetCompanyProfileByUserIdQuery(Long userId) {
    public GetCompanyProfileByUserIdQuery {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId must be positive");
    }
}
