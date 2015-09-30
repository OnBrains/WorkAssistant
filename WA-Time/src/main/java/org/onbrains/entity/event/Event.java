package org.onbrains.entity.event;

import org.onbrains.entity.SuperClass;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Calendar;
import java.util.Date;

import static org.onbrains.entity.event.EventState.END;

/**
 * @author Naumov Oleg on 18.04.2015 14:59. События которые могут происходить в течении рабочего дня. Описание
 *         {@linkplain EventType типов событий}. Одно событие может быть привязано к рабочим дня нескольких работников.
 *         Совместные командировки, совещания, миттинги команд и тд. <br/>
 *         Для {@linkplain EventType#getCategory() событий}, время которых влияет на отработанное. Не могут пересекаться
 *         рабочие интервалы. Границы интервала определяются {@linkplain #startTime началом события} и
 *         {@linkplain #endTime окончанием события}.
 */
@Entity
@Table(name = "EVENT")
public class Event extends SuperClass {

	@Temporal(TemporalType.DATE)
	@Column(name = "DAY", nullable = false)
	private Date day;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TYPE_ID", nullable = false)
	private EventType type;

	@Column(name = "TITLE", nullable = false, length = 128)
	private String title;

	@Column(name = "DESCRIPTION", nullable = true, length = 512)
	private String description;

	@Column(name = "START_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar startTime;

	@Column(name = "END_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar endTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATE", nullable = false, length = 16)
	private EventState state;

	protected Event() {
	}

	public Event(Date day, EventType type, String title, Calendar startTime) {
		this.day = day;
		this.type = type;
		this.title = title;
		this.startTime = startTime;
		this.endTime = startTime;
		this.state = EventState.NOT_END;
	}

	/**
	 * Services
	 */

	public Long getWorkedTime() {
		if (getState().equals(EventState.END)) {
			return calculationWorkedTime();
		}
		return 0L;
	}

	/**
	 * Вычисляет время, которое пойдет в учет отработанного времени за день. На отработанное время влияют только
	 * {@linkplain EventType#getCategory() определенные типы событий}. Если событие не влияет на рабочее время вернется
	 * нулевое значение.
	 *
	 * @return Отработанное время в миллисекундах.
	 */
	private Long calculationWorkedTime() {
		switch (type.getCategory()) {
		case INFLUENCE_ON_WORKED_TIME:
			if (state.equals(END)) {
				return endTime.getTimeInMillis() - startTime.getTimeInMillis();
			} else {
				long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
				return currentTimeInMillis - startTime.getTimeInMillis();
			}
		case WITH_FIXED_WORKED_TIME:
			return state.equals(END) ? type.getNotWorkingTime() : 0L;
		default:
			return 0L;
		}
	}

	/**
	 * Simple getters & setters
	 */

	/**
	 * @return День, в который происходит событие.
	 */
	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	/**
	 * @return Тип события.
	 */
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	/**
	 * @return Тема события.
	 */
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Подробное описание события.
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Время начала события.
	 */
	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return Время окончания события.
	 */
	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public EventState getState() {
		return state;
	}

	public void setState(EventState state) {
		this.state = state;
	}

}