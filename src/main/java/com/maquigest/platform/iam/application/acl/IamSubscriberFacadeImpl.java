package com.maquigest.platform.iam.application.acl;

import com.maquigest.platform.iam.application.ports.IamSubscriberReader;
import com.maquigest.platform.iam.interfaces.acl.IamSubscriberFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Published IAM facade; persistence remains inside IAM infrastructure. */
@Service
public class IamSubscriberFacadeImpl implements IamSubscriberFacade {
    private final IamSubscriberReader reader;

    public IamSubscriberFacadeImpl(IamSubscriberReader reader) {
        this.reader = reader;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveRentalCompany(Long userId) {
        return reader.isActiveRentalCompany(userId);
    }

    /**
     * REQUIRED would silently start and complete its own transaction, releasing
     * the lock before Subscriptions could save. MANDATORY prevents that error.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean lockActiveRentalCompany(Long userId) {
        return reader.lockActiveRentalCompany(userId);
    }
}
