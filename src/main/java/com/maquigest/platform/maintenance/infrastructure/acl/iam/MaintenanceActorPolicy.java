package com.maquigest.platform.maintenance.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.valueobjects.*;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
@Component
public class MaintenanceActorPolicy {
    public record Actor(Long userId,boolean rentalCompany,boolean constructionCompany) { }
    private final UserRepository users;
    public MaintenanceActorPolicy(UserRepository users){this.users=users;}
    public Actor current(Authentication authentication){
        if(authentication==null||!authentication.isAuthenticated()||authentication.getName()==null)throw MaintenanceException.forbidden();
        return users.findByEmail(authentication.getName()).filter(u->u.getStatus()==UserStatus.ACTIVE)
            .map(u->new Actor(u.getId(),hasRole(u,Roles.ROLE_RENTAL_COMPANY),hasRole(u,Roles.ROLE_CONSTRUCTION_COMPANY)))
            .filter(a->a.rentalCompany()||a.constructionCompany()).orElseThrow(MaintenanceException::forbidden);
    }
    public Actor requireOwner(Authentication authentication){
        var actor=current(authentication);
        if(!actor.rentalCompany())throw MaintenanceException.forbidden();
        return actor;
    }
    private static boolean hasRole(User user,Roles role){return user.getRoles().stream().anyMatch(r->r.getName()==role);}
}
