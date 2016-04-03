package org.onbrains.utils.jpa.converter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by Oleg on 07.02.2016.
 */
@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

//	@Override
	public Timestamp convertToDatabaseColumn(LocalDateTime locDateTime) {
		return (locDateTime == null ? null : Timestamp.valueOf(locDateTime));
	}

//	@Override
	public LocalDateTime convertToEntityAttribute(Timestamp sqlTimestamp) {
		return (sqlTimestamp == null ? null : sqlTimestamp.toLocalDateTime());
	}

}