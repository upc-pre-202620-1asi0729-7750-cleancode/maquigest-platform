package com.maquigest.platform.profiles.application.internal.queryservices;
import com.maquigest.platform.profiles.application.queryservices.CompanyProfileQueryService;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.queries.*;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;import java.util.Optional;
@Service
@Transactional(readOnly=true)
public class CompanyProfileQueryServiceImpl implements CompanyProfileQueryService {
    private final CompanyProfileRepository repository;
    public CompanyProfileQueryServiceImpl(CompanyProfileRepository repository){this.repository=repository;}
    @Override public List<CompanyProfile> handle(GetAllCompanyProfilesQuery query){
        if(query.userId()==null)return repository.findAll();
        return repository.findByUserId(query.userId()).stream().toList();
    }
    @Override public Optional<CompanyProfile> handle(GetCompanyProfileByIdQuery query){return repository.findById(query.id());}
    @Override public Optional<CompanyProfile> handle(GetCompanyProfileByUserIdQuery query){return repository.findByUserId(query.userId());}
}
