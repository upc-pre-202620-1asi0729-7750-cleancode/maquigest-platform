package com.maquigest.platform.profiles.domain.model.queries;
public record GetAllCompanyProfilesQuery(Long userId) {
    public GetAllCompanyProfilesQuery {
        if (userId != null && userId <= 0) throw new IllegalArgumentException("userId must be positive when provided");
    }
}
