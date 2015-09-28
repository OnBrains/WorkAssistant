package org.onbrains.entity.workDay;

import static org.onbrains.entity.event.EventCategory.NOT_INFLUENCE_ON_WORKED_TIME;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.onbrains.entity.SuperClass;
import org.onbrains.entity.day.Day;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.event.EventCategory;
import org.onbrains.entity.worker.Worker;

/**
 * @author Naumov Oleg on 21.03.2015 21:20.
 */

@Entity
@Table(name = "WORK_DAY", uniqueConstraints = { @UniqueConstraint(columnNames = { "WORKER_ID", "DAY_ID" }) })
@NamedQueries({
		@NamedQuery(name = WorkDay.GET_WORK_DAYS_BY_MONTH, query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day.day, 'yyyyMM') = to_char(:month, 'yyyyMM') order by wt.day"),
		@NamedQuery(name = WorkDay.GET_CURRENT_DAY, query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day.day, 'yyyyMMdd') = to_char(:day, 'yyyyMMdd')"),
		@NamedQuery(name = WorkDay.GET_DAYS_PRIOR_CURRENT_DAY, query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day, 'yyyyMM') = to_char(:month, 'yyyyMM') and wt.day <= :month order by wt.day") })
public class WorkDay extends SuperClass {

	public static final String GET_WORK_DAYS_BY_MONTH = "WorkTimeDAO.getWorkDaysByMonth";
	public static final String GET_CURRENT_DAY = "WorkTimeDAO.getCurrentDay";
	public static final String GET_DAYS_PRIOR_CURRENT_DAY = "WorkTimeDAO.getDaysPriorCurrentDay";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WORKER_ID", nullable = false)
	private Worker worker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DAY_ID", nullable = false)
	private Day day;

	@Column(name = "COMING_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar comingTime;

	@Column(name = "OUT_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar outTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATE", length = 16, nullable = false)
	private WorkDayState state = WorkDayState.NO_WORK;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.REMOVE })
	@JoinTable(name = "WORK_DAY_EVENT", joinColumns = { @JoinColumn(name = "WORK_DAY_ID") }, inverseJoinColumns = {
			@JoinColumn(name = "EVENT_ID") })
	@OrderBy(value = "startTime DESC")
	private List<Event> events = new ArrayList<>();

	@Transient
	private Event lastEvent;

	@Transient
	private Event lastWorkEvent;

	protected WorkDay() {
	}

	public WorkDay(Worker worker, Day day) {
		this.worker = worker;
		this.day = day;
		// TODO: надо ли сетить состояние тут?
		this.state = WorkDayState.NO_WORK;
	}

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
	public Calendar getComingTime() {
		return comingTime;
	}

	public void setComingTime(Calendar comingTime) {
		this.comingTime = comingTime;
	}

	/**
	 * Время окончания рабочего дня. Берётся максимальное время ухода из всех {@linkplain Event событий},
	 * {@linkplain org.onbrains.entity.event.EventType#getCategory() время которых идет в зачет отработанного}
	 *
	 * @return Время окончания рабочего дня.
	 */
	public Calendar getOutTime() {
		return outTime;
	}

	public void setOutTime(Calendar outTime) {
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

	public Event getLastEvent() {
		return !events.isEmpty() ? events.get(0) : null;
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

	public List<Event> addEvent(Event additionEvent) {
		events.add(additionEvent);
		Collections.sort(events, new EventComparator());
		return events;
	}

	/**
	 * Проверяет нужно ли изменить {@linkplain #getComingTime() время начала РД} на новое.
	 * 
	 * @param time
	 *            время, для которого выполняется проверка.
	 * @return <strong>true</strong> - если надо изменить.
	 */
	public boolean needChangeComingTimeTo(Calendar time) {
		return comingTime != null && time.getTimeInMillis() < comingTime.getTimeInMillis();
	}

	/**
	 * Проверяет нужно ли изменить {@linkplain #getOutTime() время окончания РД} на новое.
	 * 
	 * @param time
	 *            время, для которого выполняется проверка.
	 * @return <strong>true</strong> - если надо изменить.
	 */
	public boolean needChangeOutTimeTo(Calendar time) {
		return outTime != null && time.getTimeInMillis() > outTime.getTimeInMillis();
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
	public boolean isPossibleTimeBoundaryForEvent(Calendar startTime, Calendar endTime) {
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
	private boolean intervalInsideEvent(Event event, Calendar startTime, Calendar endTime) {
		boolean startTimeInsideInterval = event.getStartTime().getTimeInMillis() < startTime.getTimeInMillis()
				&& startTime.getTimeInMillis() < event.getEndTime().getTimeInMillis();
		boolean endTimeInsideInterval = event.getStartTime().getTimeInMillis() < endTime.getTimeInMillis()
				&& endTime.getTimeInMillis() < event.getEndTime().getTimeInMillis();
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
	private boolean eventInsideInterval(Event event, Calendar startTime, Calendar endTime) {
		boolean startEventTimeInsideInterval = startTime.getTimeInMillis() < event.getStartTime().getTimeInMillis()
				&& event.getStartTime().getTimeInMillis() < endTime.getTimeInMillis();
		boolean endEventTimeInsideInterval = startTime.getTimeInMillis() < event.getEndTime().getTimeInMillis()
				&& event.getEndTime().getTimeInMillis() < endTime.getTimeInMillis();
		return startEventTimeInsideInterval || endEventTimeInsideInterval;
	}

	/**
	 * Суммарное отработанное время в миллисекундах за все {@linkplain org.onbrains.entity.event.EventType#getCategory()
	 * события}, которые влияют на отработанное время.
	 *
	 * @return Суммарное отработанное время в миллисекундах.
	 */
	public Long getSummaryWorkedTime() {
		Long summaryWorkedTime = 0L;
		if (!events.isEmpty()) {
			for (Event event : events) {
				summaryWorkedTime = summaryWorkedTime + event.getWorkedTime();
			}
		}
		return summaryWorkedTime;
	}

	/**
	 * Определяет отработано ли все положенное время, за текущий рабочий день. Для каждого типа рабочего дня может быть
	 * своя {@linkplain org.onbrains.entity.workDay.DayType#getWorkTimeInMSecond() номра времени}, которое положено
	 * отработать.
	 *
	 * @return <strong>true</strong> если отработанно все положенное время.
	 */
	public boolean isWorkedFullDay() {
		return getSummaryWorkedTime() > day.getType().getWorkTimeInMSecond();
	}

	/**
	 * Вычисляет переработанное или недоработанное время, для завершенного дня.
	 *
	 * @return Переработанное/недоработанное время в миллисекундах, если день не закончен вернет 0.
	 */
	public Long getDeltaTime() {
		Long resultWorkedTimeInMSecond = Math.abs(getSummaryWorkedTime() - day.getType().getWorkTimeInMSecond());
		return getState().equals(WorkDayState.WORKED) ? resultWorkedTimeInMSecond : 0L;
	}

	private class EventComparator implements Comparator<Event> {

		@Override
		public int compare(Event first, Event second) {
			return second.getStartTime().compareTo(first.getStartTime());
		}

	}

}