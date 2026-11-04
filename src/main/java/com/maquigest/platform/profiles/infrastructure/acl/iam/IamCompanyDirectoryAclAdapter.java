package com.maquigest.platform.profiles.infrastructure.acl.iam;
import com.maquigest.platform.profiles.application.ports.IamCompanyDirectoryPort;
import com.maquigest.platform.iam.domain.model.valueobjects.*;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
/** IAM ACL: no IAM entity escapes into Profiles Application/Domain. */
@Component @Transactional(readOnly=true)
public class IamCompanyDirectoryAclAdapter implements IamCompanyDirectoryPort {
    private final UserPersistenceRepository users;
    public IamCompanyDirectoryAclAdapter(UserPersistenceRepository users){this.users=users;}
    @Override public Optional<CompanyIdentity> findActiveCompany(Long id){
        if(id==null||id<=0)return Optional.empty();
        return users.findById(id).filter(IamCompanyDirectoryAclAdapter::eligible)
            .map(IamCompanyDirectoryAclAdapter::snapshot);
    }
    @Override public List<CompanyIdentity> allActiveCompanies(){
        return users.findAll().stream().filter(IamCompanyDirectoryAclAdapter::eligible)
            .map(IamCompanyDirectoryAclAdapter::snapshot).toList();
    }
    private static boolean eligible(UserPersistenceEntity u){
        return u.getStatus()==UserStatus.ACTIVE && u.getRoles().stream().anyMatch(r ->
            r.getName()==Roles.ROLE_RENTAL_COMPANY || r.getName()==Roles.ROLE_CONSTRUCTION_COMPANY);
    }
    private static CompanyIdentity snapshot(UserPersistenceEntity u){
        return new CompanyIdentity(u.getId(),u.getFirstName(),u.getLastName(),u.getEmail(),u.getCompanyName());
    }
}
