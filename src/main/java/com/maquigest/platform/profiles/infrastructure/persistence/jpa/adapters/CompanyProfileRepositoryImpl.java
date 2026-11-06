package com.maquigest.platform.profiles.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.repositories.CompanyProfilePersistenceRepository;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.entities.CompanyProfilePersistenceEntity;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.assemblers.CompanyProfilePersistenceAssembler;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public class CompanyProfileRepositoryImpl implements CompanyProfileRepository {
    private final CompanyProfilePersistenceRepository jpa;
    private final DomainEventPublisher events;
    public CompanyProfileRepositoryImpl(CompanyProfilePersistenceRepository jpa,DomainEventPublisher events){
        this.jpa=jpa;this.events=events;
    }
    @Override public CompanyProfile save(CompanyProfile profile){
        boolean isNew=profile.getId()==null;
        var current=isNew?new CompanyProfilePersistenceEntity():jpa.findById(profile.getId())
            .orElseThrow(()->new IllegalStateException("Profile no longer exists"));
        var stored=jpa.saveAndFlush(CompanyProfilePersistenceAssembler.toPersistence(profile,current));
        if(isNew){profile.assignId(stored.getId());profile.onCreated();}
        events.publishAndClearEvents(profile);
        return CompanyProfilePersistenceAssembler.toDomain(stored);
    }
    @Override public Optional<CompanyProfile> findById(Long id){
        return jpa.findById(id).map(CompanyProfilePersistenceAssembler::toDomain);
    }
    @Override public Optional<CompanyProfile> findByUserId(Long userId){
        return jpa.findByUserId(userId).map(CompanyProfilePersistenceAssembler::toDomain);
    }
    @Override public boolean existsByUserId(Long userId){return jpa.existsByUserId(userId);}
    @Override public List<CompanyProfile> findAll(){
        return jpa.findAll().stream().map(CompanyProfilePersistenceAssembler::toDomain).toList();
    }
}
