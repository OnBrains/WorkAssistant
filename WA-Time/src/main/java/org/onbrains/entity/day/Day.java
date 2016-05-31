package org.onbrains.entity.day;

import java.time.LocalDate;

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
import javax.persistence.UniqueConstraint;

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
		@NamedQuery(name = Day.GET_DAYS_BY_MONTH, query = "select d from Day d where to_char(d.date, 'yyyyMM') = to_char(:month, 'yyyyMM')") })
//@BatchSize(size = 31)
public class Day extends SuperClass {

    private static final long serialVersionUID = 1L;

	public static final String GET_DAYS_BY_MONTH = "getDaysByMonth";

	@Column(name = "DAY", nullable = false)
    @Convert(converter = LocalDateAttributeConverter.class)
	private LocalDate date;

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE", nullable = false, length = 16)
	private DayType type;

	public Day() {
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public DayType getType() {
		return type;
	}

	public void setType(DayType type) {
		this.type = type;
	}

}
