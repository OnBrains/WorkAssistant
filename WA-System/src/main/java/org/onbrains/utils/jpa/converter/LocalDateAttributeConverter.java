package org.onbrains.utils.jpa.converter;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Конвертер для преобразования <i><b>LocalDate</b></i> в <i><b>java.util.Date</b></i>, что позволяет работать с датами
 * в <i><b>JPA</b></i>
 * <p/>
 * Created by Oleg Naumov on 07.02.2016.
 */
@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, Date> {

//	@Override
	public Date convertToDatabaseColumn(LocalDate locDate) {
		return (locDate == null ? null : Date.valueOf(locDate));
	}

//	@Override
	public LocalDate convertToEntityAttribute(Date sqlDate) {
		return (sqlDate == null ? null : sqlDate.toLocalDate());
	}

}