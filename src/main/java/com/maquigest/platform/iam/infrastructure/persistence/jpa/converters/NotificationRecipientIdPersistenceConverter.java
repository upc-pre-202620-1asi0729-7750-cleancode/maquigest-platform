package com.maquigest.platform.iam.infrastructure.persistence.jpa.converters;

import com.maquigest.platform.iam.domain.model.valueobjects.NotificationRecipientId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NotificationRecipientIdPersistenceConverter implements AttributeConverter<NotificationRecipientId, Long> {
    @Override
    public Long convertToDatabaseColumn(NotificationRecipientId attribute) {
        return attribute == null ? null : attribute.recipientId();
    }

    @Override
    public NotificationRecipientId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new NotificationRecipientId(dbData);
    }
}
