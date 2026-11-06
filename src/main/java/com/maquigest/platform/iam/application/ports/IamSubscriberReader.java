package com.maquigest.platform.iam.application.ports;

/** IAM-owned outbound port for checking and locking a subscriber row. */
public interface IamSubscriberReader {
    boolean isActiveRentalCompany(Long userId);
    boolean lockActiveRentalCompany(Long userId);
}
