package com.maquigest.platform.subscriptions.application.ports;
/** ACL port for IAM identity and role checks, without IAM models leaking into Domain. */
public interface IamSubscriberPort {
    boolean isActiveRentalCompany(Long userId);
    /** Lock the existing IAM user row to serialize concurrent subscription changes per subscriber. */
    boolean lockActiveRentalCompany(Long userId);
}
