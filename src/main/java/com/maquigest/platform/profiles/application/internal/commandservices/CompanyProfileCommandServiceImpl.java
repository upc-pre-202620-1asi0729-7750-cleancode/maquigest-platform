package com.maquigest.platform.profiles.application.internal.commandservices;
import com.maquigest.platform.profiles.application.commandservices.CompanyProfileCommandService;
import com.maquigest.platform.profiles.application.ports.IamCompanyDirectoryPort;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.*;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import com.maquigest.platform.shared.application.result.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class CompanyProfileCommandServiceImpl implements CompanyProfileCommandService {
    private final CompanyProfileRepository repository;
    private final IamCompanyDirectoryPort directory;
    public CompanyProfileCommandServiceImpl(CompanyProfileRepository repository,IamCompanyDirectoryPort directory){
        this.repository=repository;this.directory=directory;
    }
    @Override @Transactional
    public Result<CompanyProfile,ApplicationError> handle(CreateCompanyProfileCommand command){
        if(command==null||command.userId()==null||command.userId()<=0)
            return Result.failure(ApplicationError.validationError("userId","Positive userId required"));
        if(directory.findActiveCompany(command.userId()).isEmpty())
            return Result.failure(ApplicationError.notFound("IamCompany",String.valueOf(command.userId())));
        if(repository.existsByUserId(command.userId()))
            return Result.failure(ApplicationError.conflict("Profile","One company profile per IAM user"));
        try{return Result.success(repository.save(new CompanyProfile(command)));}
        catch(IllegalArgumentException ex){return Result.failure(ApplicationError.validationError("profile",ex.getMessage()));}
    }
    @Override @Transactional
    public Result<CompanyProfile,ApplicationError> handle(UpdateCompanyProfileCommand command){
        if(command==null||command.id()==null||command.id()<=0)
            return Result.failure(ApplicationError.validationError("id","Positive id required"));
        var existing=repository.findById(command.id());
        if(existing.isEmpty())return Result.failure(ApplicationError.notFound("Profile",String.valueOf(command.id())));
        if(!Objects.equals(existing.get().getUserId(),command.userId()))
            return Result.failure(ApplicationError.validationError("userId","Profile userId is immutable"));
        if(directory.findActiveCompany(command.userId()).isEmpty())
            return Result.failure(ApplicationError.notFound("IamCompany",String.valueOf(command.userId())));
        try{
            var profile=existing.get();profile.update(command);
            return Result.success(repository.save(profile));
        }catch(IllegalArgumentException ex){return Result.failure(ApplicationError.validationError("profile",ex.getMessage()));}
    }
    /** Idempotent projection from IAM; never writes to an existing company profile. */
    @Override @Transactional
    public int provisionMissingFromIam(){
        int count=0;
        for(var user:directory.allActiveCompanies()){
            if(repository.existsByUserId(user.userId()))continue;
            var profile=new CompanyProfile(new CreateCompanyProfileCommand(user.userId(),
                user.firstName(),user.lastName(),user.email(),"",user.companyName(),CompanyAddress.empty()));
            repository.save(profile);count++;
        }
        return count;
    }
}
