package com.maquigest.platform.rentals.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.valueobjects.*;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
@Component
public class RentalActorPolicy {
    public record Actor(Long userId,boolean constructionCompany,boolean rentalCompany) { }
    private final UserRepository users;
    public RentalActorPolicy(UserRepository users){this.users=users;}
    public Optional<Actor> current(Authentication auth) {
        if(auth==null || !auth.isAuthenticated() || auth.getName()==null) return Optional.empty();
        return users.findByEmail(auth.getName()).filter(u->u.getStatus()==UserStatus.ACTIVE)
           .map(u->new Actor(u.getId(),hasRole(u,Roles.ROLE_CONSTRUCTION_COMPANY),hasRole(u,Roles.ROLE_RENTAL_COMPANY)))
           .filter(a->a.constructionCompany()||a.rentalCompany());
    }
    private static boolean hasRole(User u,Roles role) {
        return u.getRoles().stream().anyMatch(r->r.getName()==role);
    }
}
