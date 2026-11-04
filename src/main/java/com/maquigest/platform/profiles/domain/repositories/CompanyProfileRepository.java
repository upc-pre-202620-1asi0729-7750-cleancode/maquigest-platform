package com.maquigest.platform.profiles.domain.repositories;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import java.util.List;import java.util.Optional;
public interface CompanyProfileRepository {
    CompanyProfile save(CompanyProfile profile);
    Optional<CompanyProfile> findById(Long id);
    Optional<CompanyProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<CompanyProfile> findAll();
}
