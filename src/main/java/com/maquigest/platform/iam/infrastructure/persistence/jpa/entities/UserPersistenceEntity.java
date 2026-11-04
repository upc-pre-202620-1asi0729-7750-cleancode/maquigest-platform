package com.maquigest.platform.iam.infrastructure.persistence.jpa.entities;

import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/** JPA representation of IAM User. No persistence annotations appear in the aggregate. */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password", nullable = false, length = 120)
    private String password;

    @Column(name = "company_name", nullable = false, length = 180)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RolePersistenceEntity> roles = new HashSet<>();
}
