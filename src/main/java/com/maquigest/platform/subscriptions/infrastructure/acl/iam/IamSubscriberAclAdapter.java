package com.maquigest.platform.subscriptions.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamSubscriberFacade;
import com.maquigest.platform.subscriptions.application.ports.IamSubscriberPort;
import org.springframework.stereotype.Component;

/** Consumer-side ACL: Subscriptions depends only on IAM's published contract. */
@Component
public class IamSubscriberAclAdapter implements IamSubscriberPort {
    private final IamSubscriberFacade iamSubscriberFacade;

    public IamSubscriberAclAdapter(IamSubscriberFacade iamSubscriberFacade) {
        this.iamSubscriberFacade = iamSubscriberFacade;
    }

    @Override
    public boolean isActiveRentalCompany(Long userId) {
        return iamSubscriberFacade.isActiveRentalCompany(userId);
    }

    @Override
    public boolean lockActiveRentalCompany(Long userId) {
        return iamSubscriberFacade.lockActiveRentalCompany(userId);
    }
}
