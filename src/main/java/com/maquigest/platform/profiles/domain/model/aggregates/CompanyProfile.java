package com.maquigest.platform.profiles.domain.model.aggregates;
import com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.commands.UpdateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileCreatedEvent;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileUpdatedEvent;
import com.maquigest.platform.profiles.domain.model.valueobjects.*;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.util.Objects;

/** Company profile aggregate. IAM userId is a reference, not a foreign aggregate. */
public class CompanyProfile extends AbstractDomainAggregateRoot<CompanyProfile> {
    private Long id;
    private final Long userId;
    private PersonName name;
    private EmailAddress contactEmail;
    private String phoneNumber;
    private String companyName;
    private CompanyAddress address;

    public CompanyProfile(CreateCompanyProfileCommand command) {
        if(command==null)throw new IllegalArgumentException("command is required");
        this.userId=validId(command.userId(),"userId");
        setFields(command.firstName(),command.lastName(),command.contactEmail(),
            command.phoneNumber(),command.companyName(),command.address());
    }
    public static CompanyProfile reconstitute(Long id,CreateCompanyProfileCommand data){
        var result=new CompanyProfile(data);result.id=validId(id,"id");return result;
    }
    public void assignId(Long id){
        if(this.id!=null && !this.id.equals(id)) throw new IllegalStateException("profile id cannot be changed");
        this.id=validId(id,"id");
    }
    public void onCreated(){
        if(id==null)throw new IllegalStateException("persist profile before publishing its creation");
        registerDomainEvent(new CompanyProfileCreatedEvent(id,userId));
    }
    public void update(UpdateCompanyProfileCommand command){
        Objects.requireNonNull(command,"command");
        if(!Objects.equals(id,command.id()) || !userId.equals(command.userId()))
            throw new IllegalArgumentException("profile ownership and identity cannot be changed");
        setFields(command.firstName(),command.lastName(),command.contactEmail(),
            command.phoneNumber(),command.companyName(),command.address());
        registerDomainEvent(new CompanyProfileUpdatedEvent(id,userId));
    }
    private void setFields(String firstName,String lastName,String email,String phone,String company,
                           CompanyAddress address){
        this.name=new PersonName(firstName,lastName);
        this.contactEmail=new EmailAddress(email);
        this.phoneNumber=phone==null?"":phone.trim();
        if(this.phoneNumber.length()>40)throw new IllegalArgumentException("phoneNumber exceeds 40 characters");
        if(company==null || company.isBlank() || company.trim().length()>180)
            throw new IllegalArgumentException("companyName is required (max 180)");
        this.companyName=company.trim();
        this.address=Objects.requireNonNull(address,"address is required");
    }
    private static Long validId(Long v,String field){
        if(v==null||v<=0)throw new IllegalArgumentException(field+" must be positive");return v;
    }
    public Long getId(){return id;}
    public Long getUserId(){return userId;}
    public String getFirstName(){return name.firstName();}
    public String getLastName(){return name.lastName();}
    public String getContactEmail(){return contactEmail.value();}
    public String getPhoneNumber(){return phoneNumber;}
    public String getCompanyName(){return companyName;}
    public CompanyAddress getAddress(){return address;}
}
