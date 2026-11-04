package com.maquigest.platform.profiles.infrastructure.persistence.jpa.entities;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.embeddables.CompanyAddressPersistenceEmbeddable;
import jakarta.persistence.*;
import lombok.Getter;import lombok.Setter;import lombok.NoArgsConstructor;
/** Store IAM userId as a scalar reference; no cross-bounded-context JPA relation. */
@Entity @Table(name="company_profiles",uniqueConstraints={
    @UniqueConstraint(name="uk_company_profiles_user_id",columnNames="user_id")},indexes={
    @Index(name="idx_company_profiles_company_name",columnList="company_name")})
@Getter @Setter @NoArgsConstructor
public class CompanyProfilePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="first_name",nullable=false,length=100) private String firstName;
    @Column(name="last_name",nullable=false,length=150) private String lastName;
    @Column(name="contact_email",nullable=false,length=254) private String contactEmail;
    @Column(name="phone_number",nullable=false,length=40) private String phoneNumber;
    @Column(name="company_name",nullable=false,length=180) private String companyName;
    @Embedded private CompanyAddressPersistenceEmbeddable address = new CompanyAddressPersistenceEmbeddable();
    @Version @Column(nullable=false) private Long version;
}
