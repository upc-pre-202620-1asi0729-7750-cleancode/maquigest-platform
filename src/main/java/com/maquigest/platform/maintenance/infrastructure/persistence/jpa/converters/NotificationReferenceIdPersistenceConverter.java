package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.converters;

import com.maquigest.platform.maintenance.domain.model.valueobjects.NotificationReferenceId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NotificationReferenceIdPersistenceConverter implements AttributeConverter<NotificationReferenceId, Long> {
    @Override
    public Long convertToDatabaseColumn(NotificationReferenceId attribute) {
        return attribute == null ? null : attribute.referenceId();
    }

    @Override
    public NotificationReferenceId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new NotificationReferenceId(dbData);
    }
}
