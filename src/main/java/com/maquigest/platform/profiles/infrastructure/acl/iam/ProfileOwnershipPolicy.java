package com.maquigest.platform.profiles.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.valueobjects.*;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Objects;
/** Never trust userId sent in a request instead of the authenticated IAM identity. */
@Component
public class ProfileOwnershipPolicy {
    private final UserRepository users;
    private final CompanyProfileRepository profiles;
    public ProfileOwnershipPolicy(UserRepository users,CompanyProfileRepository profiles){
        this.users=users;this.profiles=profiles;
    }
    public boolean canCreate(Authentication auth,Long userId){return owns(auth,userId);}
    public boolean canModify(Authentication auth,Long profileId){
        if(profileId==null||profileId<=0)return false;
        return profiles.findById(profileId).map(p->owns(auth,p.getUserId())).orElse(false);
    }
    private boolean owns(Authentication auth,Long targetUserId){
        if(auth==null||!auth.isAuthenticated()||auth.getName()==null)return false;
        return users.findByEmail(auth.getName()).filter(u->u.getStatus()==UserStatus.ACTIVE)
            .filter(u->u.getRoles().stream().anyMatch(r->
                r.getName()==Roles.ROLE_CONSTRUCTION_COMPANY||r.getName()==Roles.ROLE_RENTAL_COMPANY))
            .map(u->Objects.equals(u.getId(),targetUserId)).orElse(false);
    }
}
