package com.maquigest.platform.iam.domain.model.aggregates;

import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/** IAM aggregate root; independent from JPA and HTTP. */
@Getter
public class User extends AbstractDomainAggregateRoot<User> {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String companyName;
    private Set<Role> roles;
    private UserStatus status;

    public User(String firstName, String lastName, String email, String password,
                String companyName, Role role) {
        this.firstName = requireNonBlank(firstName, "firstName");
        this.lastName = requireNonBlank(lastName, "lastName");
        this.email = requireNonBlank(email, "email").toLowerCase(java.util.Locale.ROOT);
        if (!this.email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("email must be valid");
        }
        this.password = requireNonBlank(password, "password");
        this.companyName = requireNonBlank(companyName, "companyName");
        if (role == null || role.getName() == null) {
            throw new IllegalArgumentException("role is required");
        }
        this.roles = new HashSet<>();
        this.roles.add(role);
        this.status = UserStatus.ACTIVE;
    }

    public static User reconstitute(Long id, String firstName, String lastName, String email,
                                    String password, String companyName, Set<Role> roles,
                                    UserStatus status) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
        var user = new User(firstName, lastName, email, password,
                companyName, roles.iterator().next());
        user.id = id;
        user.roles = new HashSet<>(roles);
        user.status = status == null ? UserStatus.ACTIVE : status;
        return user;
    }

    public User addRole(Role role) {
        if (role == null || role.getName() == null) {
            throw new IllegalArgumentException("role is required");
        }
        this.roles.add(role);
        return this;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
