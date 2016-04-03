package org.onbrains.entity.workDay;

import static org.onbrains.entity.event.EventCategory.INFLUENCE_ON_WORKED_TIME;
import static org.onbrains.entity.event.EventCategory.NOT_INFLUENCE_ON_WORKED_TIME;
import static org.onbrains.entity.workDay.WorkDayState.WORKED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.onbrains.entity.SuperClass;
import org.onbrains.entity.day.Day;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.event.EventCategory;
import org.onbrains.entity.worker.Worker;
import org.onbrains.utils.parsing.DateFormatService;
import org.onbrains.utils.jpa.converter.LocalDateTimeAttributeConverter;

/**
 * @author Naumov Oleg on 21.03.2015 21:20.
 */

@Entity
@Table(name = "WORK_DAY", uniqueConstraints = { @UniqueConstraint(columnNames = { "WORKER_ID", "DAY_ID" }) })
@NamedQueries({
		@NamedQuery(name = WorkDay.GET_WORK_DAYS_BY_MONTH, query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day.day, 'yyyyMM') = :month) order by wt.day"),
		@NamedQuery(name = WorkDay.GET_WORK_DAY, query = "select wt from WorkDay wt where wt.worker = :worker and wt.day = :day") })
public class WorkDay extends SuperClass {

    private static final long serialVersionUID = 1L;

	public static final String GET_WORK_DAYS_BY_MONTH = "WorkTimeDAO.getWorkDaysByMonth";
	public static final String GET_WORK_DAY = "WorkTimeDAO.getWorkDay";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WORKER_ID", nullable = false)
	private Worker worker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DAY_ID", nullable = false)
	private Day day;

	@Column(name = "COMING_TIME")
//	@Temporal(TemporalType.TIMESTAMP)
    @Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime comingTime;

	@Column(name = "OUT_TIME")
//	@Temporal(TemporalType.TIMESTAMP)
    @Convert(converter = LocalDateTimeAttributeConverter.class)
	private LocalDateTime outTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATE", length = 16, nullable = false)
	private WorkDayState state = WorkDayState.NO_WORK;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.REMOVE })
	@JoinTable(name = "WORK_DAY_EVENT", joinColumns = { @JoinColumn(name = "WORK_DAY_ID") }, inverseJoinColumns = {
			@JoinColumn(name = "EVENT_ID") })
	@OrderBy(value = "startTime desc")
	private List<Event> events = new ArrayList<>();

	protected WorkDay() {
	}

	public WorkDay(Worker worker, Day day) {
		this.worker = worker;
		this.day = day;
		this.state = WorkDayState.NO_WORK;
	}

	// *****************************************************************************************************************
	// Service methods
	// *****************************************************************************************************************

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "Не работал".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "Не работал".
	 */
	public boolean isNoWork() {
		return getState().equals(WorkDayState.NO_WORK);
	}

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "На работе".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "На работе".
	 */
	public boolean isWorking() {
		return getState().equals(WorkDayState.WORKING);
	}

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "Отработал".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "Отработал".
	 */
	public boolean isWorked() {
		return getState().equals(WORKED);
	}

	public Event getLastWorkEvent() {
		if (!events.isEmpty()) {
			for (Event event : events) {
				if (!event.getType().getCategory().equals(NOT_INFLUENCE_ON_WORKED_TIME)) {
					return event;
				}
			}
		}
		return null;
	}

	/**
	 * Добавляет событие в список событий рабочего дня. Если событие влияет на рабочее время, то осуществляется
	 * проверка, что нет пересечений с другими событиями, а так же изменяется время {@linkplain #comingTime начала РД} и
	 * {@linkplain #outTime окончания РД}
	 *
	 * @param additionEvent
	 *            добавляемое событие.
	 * @return Сообщение об ошибки, если невозможно добавить событие или пустую строку если событие добавлено.
	 */
	public String addEvent(Event additionEvent) {
		if (!additionEvent.getType().getCategory().equals(NOT_INFLUENCE_ON_WORKED_TIME)) {
			if (isPossibleTimeBoundaryForEvent(additionEvent.getStartTime(), additionEvent.getEndTime())) {
				changeTimeBy(additionEvent);
			} else {
				return "Пересечение временых интервалов у событий";
			}
		}
		events.add(additionEvent);
		Collections.sort(events, new EventComparator());
		return "";
	}

	/**
	 * Изменяет время начала и окончания РД если это необходимо. Время может изменится при добавлении нового события или
	 * изменении существующего. При этом на время РД влияют только {@link EventCategory#INFLUENCE_ON_WORKED_TIME}
	 * события.
	 *
	 * @param event
	 *            событие из-за которого могло изменится время.
	 */
	public void changeTimeBy(Event event) {
		if (event.getType().getCategory().equals(INFLUENCE_ON_WORKED_TIME)) {
			if (needChangeComingTimeTo(event.getStartTime())) {
				setComingTime(event.getStartTime());
			}
			if (needChangeOutTimeTo(event.getEndTime())) {
				setOutTime(event.getEndTime());
			}
		}
	}

	/**
	 * Проверяет возможна ли данная граница временого интервала. Для {@linkplain EventCategory категорий} событий,
	 * влияющих на отработаное время временые интервалы пересекаться не должны.
	 *
	 * @param startTime
	 *            время начала.
	 * @param endTime
	 *            время окончания.
	 * @return <strong>true</strong> - если данная граница допустима.
	 */
	public boolean isPossibleTimeBoundaryForEvent(LocalDateTime startTime, LocalDateTime endTime) {
		if (!events.isEmpty())
			for (Event event : events) {
				if (!event.getType().getCategory().equals(EventCategory.NOT_INFLUENCE_ON_WORKED_TIME)) {
					if (intervalInsideEvent(event, startTime, endTime)
							|| eventInsideInterval(event, startTime, endTime)) {
						return false;
					}
				}
			}
		return true;
	}

	/**
	 * Суммарное отработанное время в миллисекундах за все {@linkplain org.onbrains.entity.event.EventType#getCategory()
	 * события}, которые влияют на отработанное время.
	 *
	 * @return Суммарное отработанное время в миллисекундах.
	 */
	public Long getWorkingTime() {
		return calculateWorkedTime();
	}

	public Long getWorkedTime() {
		return state.equals(WORKED) ? calculateWorkedTime() : 0;
	}

	public Long getIdealWorkedTime() {
		return getDay().getType().getWorkTimeInSecond();
	}

	/**
	 * Определяет отработано ли все положенное время, за текущий рабочий день. Для каждого типа рабочего дня может быть
	 * своя {@linkplain org.onbrains.entity.workDay.DayType#getWorkTimeInSecond() номра времени}, которое положено
	 * отработать.
	 *
	 * @return <strong>true</strong> если отработанно все положенное время.
	 */
	public boolean isWorkedFullDay() {
		return getWorkedTime() > day.getType().getWorkTimeInSecond();
	}

	public boolean isWorkingRequiredTime() {
		return getWorkingTime() > day.getType().getWorkTimeInSecond();
	}

	/**
	 * Вычисляет переработанное или недоработанное время.
	 *
	 * @return Переработанное/недоработанное время в миллисекундах, если день не закончен вернет 0.
	 */
	public Long getDeltaTime() {
		return Math.abs(getWorkingTime() - day.getType().getWorkTimeInSecond());
	}

	public String getComingTimeValue() {
		return !isNoWork() ? DateFormatService.toHHMM(comingTime.getTime()) : "__:__";
	}

	public String getOutTimeValue() {
		return isWorked() ? DateFormatService.toHHMM(outTime.getTime()) : "__:__";
	}

	// *****************************************************************************************************************
	// Private methods
	// *****************************************************************************************************************

	/**
	 * Проверяет нужно ли изменить {@linkplain #getComingTime() время начала РД} на новое.
	 *
	 * @param time
	 *            время, для которого выполняется проверка.
	 * @return <strong>true</strong> - если надо изменить.
	 */
	private boolean needChangeComingTimeTo(LocalDateTime time) {
		return comingTime != null && time.isBefore(comingTime);
	}

	/**
	 * Проверяет нужно ли изменить {@linkplain #getOutTime() время окончания РД} на новое.
	 *
	 * @param time
	 *            время, для которого выполняется проверка.
	 * @return <strong>true</strong> - если надо изменить.
	 */
	private boolean needChangeOutTimeTo(LocalDateTime time) {
		return outTime != null && time.isAfter(outTime);
	}

	/**
	 * Проверяет находится ли время во временном интервале события.
	 *
	 * @param event
	 *            событие.
	 * @param startTime
	 *            время начала.
	 * @param endTime
	 *            время окончания.
	 * @return <strong>true</strong> - если время находится внутри интервала.
	 */
	private boolean intervalInsideEvent(Event event, LocalDateTime startTime, LocalDateTime endTime) {
		boolean startTimeInsideInterval = event.getStartTime().isBefore(startTime)
				&& event.getEndTime().isAfter(startTime);
		boolean endTimeInsideInterval = event.getStartTime().isBefore(endTime) && event.getEndTime().isAfter(endTime);
		return startTimeInsideInterval || endTimeInsideInterval;
	}

	/**
	 * Проверяет находится ли интервал события в границах интервала времени.
	 *
	 * @param event
	 *            событие.
	 * @param startTime
	 *            время начала.
	 * @param endTime
	 *            время окончания.
	 * @return <strong>true</strong> - если интервал события назодится в границах времени.
	 */
	private boolean eventInsideInterval(Event event, LocalDateTime startTime, LocalDateTime endTime) {
		boolean startEventTimeInsideInterval = startTime.isBefore(event.getStartTime())
				&& endTime.isAfter(event.getStartTime());
		boolean endEventTimeInsideInterval = startTime.isBefore(event.getEndTime())
				&& endTime.isAfter(event.getEndTime());
		return startEventTimeInsideInterval || endEventTimeInsideInterval;
	}

	private Long calculateWorkedTime() {
		Long summaryWorkedTime = 0L;
		if (!events.isEmpty()) {
			for (Event event : events) {
				summaryWorkedTime = summaryWorkedTime + event.getWorkedTime();
			}
		}
		return summaryWorkedTime;
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

	/**
	 * @return Работник, к которому относится данный рабочий день.
	 */
	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	/**
	 * @return Конкретный день в году
	 */
	public Day getDay() {
		return day;
	}

	public void setDay(Day day) {
		this.day = day;
	}

	/**
	 * Время начала рабочего дня. Берётся минимальное время прихода из всех {@linkplain Event событий},
	 * {@linkplain org.onbrains.entity.event.EventType#getCategory() время которых идет в зачет отработанного}.
	 *
	 * @return Время начала рабочего дня.
	 */
	public LocalDateTime getComingTime() {
		return comingTime;
	}

	public void setComingTime(LocalDateTime comingTime) {
		this.comingTime = comingTime;
	}

	/**
	 * Время окончания рабочего дня. Берётся максимальное время ухода из всех {@linkplain Event событий},
	 * {@linkplain org.onbrains.entity.event.EventType#getCategory() время которых идет в зачет отработанного}
	 *
	 * @return Время окончания рабочего дня.
	 */
	public LocalDateTime getOutTime() {
		return outTime;
	}

	public void setOutTime(LocalDateTime outTime) {
		this.outTime = outTime;
	}

	/**
	 * Состаяние рабочего дня. Показывает в какой стадии находится рабочий день. Состояние <strong>На работе</strong>
	 * может быть выбрано только для текущего рабочего дня.
	 *
	 * @return Состояние, в котором находится рабочий день.
	 */
	public WorkDayState getState() {
		return state;
	}

	public void setState(WorkDayState state) {
		this.state = state;
	}

	/**
	 * @return Перечень событий относящихся к рабочему дню.
	 */
	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	private class EventComparator implements Comparator<Event> {

		@Override
		public int compare(Event first, Event second) {
			return second.getStartTime().compareTo(first.getStartTime());
		}

	}

}