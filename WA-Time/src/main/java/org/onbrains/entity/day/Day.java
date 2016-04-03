package org.onbrains.entity.day;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
//import javax.persistence.Convert;
//import javax.persistence.Converter;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;
import org.onbrains.entity.SuperClass;
import org.onbrains.entity.workDay.DayType;
import org.onbrains.utils.jpa.converter.LocalDateAttributeConverter;

/**
 * @author Naumov Oleg on 27.07.2015 20:15. <br/>
 *         Конкретный день года, в дальнейшем надо сделать возможность переопределять дни для разных организаций.
 */
@Entity
@Table(name = "DAY", uniqueConstraints = @UniqueConstraint(columnNames = { "DAY" }) )
@NamedQueries({
		@NamedQuery(name = Day.GET_DAYS_BY_MONTH, query = "select d from Day d where to_char(d.day, 'yyyyMM') = to_char(:month, 'yyyyMM')") })
@BatchSize(size = 31)
public class Day extends SuperClass {

    private static final long serialVersionUID = 1L;

	public static final String GET_DAYS_BY_MONTH = "getDaysByMonth";

	@Column(name = "DAY", nullable = false)
    @Convert(converter = LocalDateAttributeConverter.class)
//    @Temporal(TemporalType.DATE)
	private LocalDate day;

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE", nullable = false, length = 16)
	private DayType type;

	public Day() {
	}

	public LocalDate getDay() {
		return day;
	}

	public void setDay(LocalDate day) {
		this.day = day;
	}

	public DayType getType() {
		return type;
	}

	public void setType(DayType type) {
		this.type = type;
	}

}
