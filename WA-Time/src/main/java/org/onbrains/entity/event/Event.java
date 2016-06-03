package org.onbrains.entity.event;

import static org.onbrains.entity.event.EventState.END;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.onbrains.entity.SuperClass;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.utils.jpa.converter.LocalDateAttributeConverter;
import org.onbrains.utils.jpa.converter.LocalDateTimeAttributeConverter;

/**
 * События которые могут происходить в течении рабочего дня. Описание {@linkplain EventType типов событий}. Одно событие
 * может быть привязано к рабочим дня нескольких работников. Совместные командировки, совещания, миттинги команд и тд.
 * <br/>
 * Для {@linkplain EventType#getCategory() событий}, время которых влияет на отработанное. Не могут пересекаться рабочие
 * интервалы. Границы интервала определяются {@linkplain #startTime началом события} и {@linkplain #endTime окончанием
 * события}.
 * <p/>
 * 
 * @author Naumov Oleg on 18.04.2015 14:59.
 */
@Entity
@Table(name = "EVENT")
public class Event extends SuperClass {

	private static final long serialVersionUID = 7713501051336165237L;

	@Column(name = "DAY", nullable = false)
	@Convert(converter = LocalDateAttributeConverter.class)
	private LocalDate day;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WORK_DAY_ID", nullable = false)
	private WorkDay workDay;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TYPE_ID", nullable = false)
	private EventType type;

	@Column(name = "TITLE", nullable = false, length = 128)
	private String title;

	@Column(name = "DESCRIPTION", nullable = true, length = 512)
	private String description;

	@Column(name = "FULL_DAY")
	private Boolean fullDay;

	@Column(name = "START_TIME", nullable = false)
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime startTime;

	@Column(name = "END_TIME", nullable = true)
	@Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATE", nullable = false, length = 16)
	private EventState state;

	protected Event() {
	}

	public Event(WorkDay workDay, EventType type, String title, LocalDateTime startTime) {
		this.day = workDay.getDay().getDate();
		this.workDay = workDay;
		this.type = type;
		this.title = title;
		this.startTime = startTime.truncatedTo(ChronoUnit.MINUTES);
		this.endTime = startTime.truncatedTo(ChronoUnit.MINUTES);
		this.state = EventState.NOT_END;
	}

	public Event(WorkDay workDay, EventType type, String title, Boolean fullDay, LocalDateTime startTime,
			LocalDateTime endTime) {
		this(workDay, type, title, startTime);
		this.fullDay = fullDay;
		this.endTime = endTime;
		this.state = EventState.END;
	}

	// *****************************************************************************************************************
	// Service methods
	// *****************************************************************************************************************

	public EventCategory getCategory() {
		return type.getCategory();
	}

	public Long getWorkedTime() {
		return calculationWorkedTime();
	}

	// FIXME WA-10
	public Date getStartTimeValue() {
		return Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public void setStartTimeValue(Date startTimeValue) {
		setStartTime(LocalDateTime.ofInstant(startTimeValue.toInstant(), ZoneId.systemDefault()));
	}

	// FIXME WA-10
	public Date getEndTimeValue() {
		return Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public void setEndTimeValue(Date endTimeValue) {
		setEndTime(LocalDateTime.ofInstant(endTimeValue.toInstant(), ZoneId.systemDefault()));
	}

	// *****************************************************************************************************************
	// Private methods
	// *****************************************************************************************************************

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
				return Duration.between(startTime, endTime).getSeconds();
			} else {
				return Duration.between(startTime, LocalDateTime.now()).getSeconds();
			}
		case WITH_FIXED_WORKED_TIME:
			return state.equals(END) ? type.getNotWorkingTime() : 0L;
		default:
			return 0L;
		}
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

	/**
	 * @return День, в который происходит событие.
	 */
	public LocalDate getDay() {
		return day;
	}

	public void setDay(LocalDate day) {
		this.day = day;
	}

	public WorkDay getWorkDay() {
		return workDay;
	}

	public void setWorkDay(WorkDay workDay) {
		this.workDay = workDay;
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
	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime.truncatedTo(ChronoUnit.MINUTES);
	}

	/**
	 * @return Время окончания события.
	 */
	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime.truncatedTo(ChronoUnit.MINUTES);
	}

	public EventState getState() {
		return state;
	}

	public void setState(EventState state) {
		this.state = state;
	}

}