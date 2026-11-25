package com.maquigest.platform.rentals.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamCompanyDirectoryFacade;
import com.maquigest.platform.rentals.application.ports.RentalNotificationRecipientPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RentalNotificationRecipientAclAdapter implements RentalNotificationRecipientPort {
    private final IamCompanyDirectoryFacade directory;

    public RentalNotificationRecipientAclAdapter(IamCompanyDirectoryFacade directory) {
        this.directory = directory;
    }

    @Override public Optional<String> findActiveEmail(Long userId) {
        return directory.findActiveCompany(userId).map(IamCompanyDirectoryFacade.CompanyIdentity::email)
                .filter(email -> !email.isBlank());
    }
}
