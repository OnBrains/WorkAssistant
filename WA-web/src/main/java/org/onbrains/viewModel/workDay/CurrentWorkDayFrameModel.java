package org.onbrains.viewModel.workDay;

import static org.onbrains.entity.event.EventState.END;
import static org.onbrains.entity.event.EventState.NOT_END;
import static org.onbrains.utils.parsing.DateFormatService.toDDEE;
import static org.onbrains.utils.parsing.DateFormatService.toHHMM;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.onbrains.component.statistic.StatisticValue;
import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.event.EventCategory;
import org.onbrains.entity.event.EventType;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.information.Notification;
import org.primefaces.event.RowEditEvent;

/**
 * @author Naumov Oleg on 04.08.2015 21:40.
 */
@Named
@ViewScoped
public class CurrentWorkDayFrameModel implements Serializable {

	@Inject
	private EntityManagerUtils em;
	@Inject
	private WorkDayDAOInterface wdDAO;
	@Inject
	private EventTypeDAOInterface etDAO;

	private WorkDay workDay;
	private EventType selectedEventType;

	@PostConstruct
	public void postConstruct() {
		if (workDay == null) {
			initCurrentWorkDay();
		}
	}

	public List<StatisticValue> getWorkDayStatistic() {
		List<StatisticValue> workDayStatistic = new LinkedList<>();
		if (workDay != null) {
			if (!workDay.isNoWork()) {
				long workedTime = workDay.isWorkingRequiredTime() ? workDay.getDay().getType().getWorkTimeInSecond()
						: workDay.getWorkingTime();
				workDayStatistic.add(new StatisticValue(workedTime, "Отработано", "#4da9f1"));
			}
			workDayStatistic.add(
					workDay.isWorkingRequiredTime() ? new StatisticValue(workDay.getDeltaTime(), "Переработок", "green")
							: new StatisticValue(workDay.getDeltaTime(), "Осталось", "chocolate"));
		}
		return workDayStatistic;
	}

	public void onRowEdit(RowEditEvent event) {
		Event editableEvent = (Event) event.getObject();
		LocalDateTime startTime = fixDate(editableEvent.getStartTime(), editableEvent.getDay());
		LocalDateTime endTime = fixDate(editableEvent.getEndTime(), editableEvent.getDay());
		if (editableEvent.getState().equals(END) && startTime.isAfter(endTime)) {
			Notification.warn("Невозможно сохранить изменения", "Время окончания события больше времени начала");
			refreshEvent(editableEvent);
			return;
		}
		if (workDay.isPossibleTimeBoundaryForEvent(startTime, endTime)) {
			editableEvent.setStartTime(startTime);
			editableEvent.setEndTime(endTime);
			workDay.changeTimeBy(editableEvent);
			em.merge(workDay);
			em.merge(editableEvent);
		} else {
			refreshEvent(editableEvent);
			Notification.warn("Невозможно сохранить изменения", "Пересечение временых интервалов у событий");
		}
	}

	/**
	 * Проверяет можно ли закончить {@linkplain WorkDay#getLastWorkEvent последнее событие}.
	 *
	 * @return <strong>true</strong> - если можно.
	 */
	public boolean canStopEvent() {
		return workDay != null && !workDay.getEvents().isEmpty()
				&& workDay.getLastWorkEvent().getState().equals(NOT_END);
	}

	/**
	 * Создает событие и отмечает начало рабочего дня если это первое событие для текущего дня. Если в момент создания
	 * нового события, {@linkplain WorkDay#getLastWorkEvent последнее событие} не закончено, то перед создание нового
	 * события, для него проставится статус "Закончено".
	 */
	public void startEvent() {
		stopLastActiveEvent();
		Event creationEvent = new Event(workDay.getDay().getDay(), selectedEventType, selectedEventType.getTitle(),
				LocalDateTime.now());
		String addEventMessage = workDay.addEvent(creationEvent);
		if (Objects.equals(addEventMessage, "")) {
			em.persist(creationEvent);
			if (workDay.isNoWork()
					&& !creationEvent.getType().getCategory().equals(EventCategory.NOT_INFLUENCE_ON_WORKED_TIME)) {
				startWork();
			}
			em.merge(workDay);
		} else {
			Notification.warn("Невозможно создать событие", addEventMessage);
		}
		cleanAfterCreation();
	}

	/**
	 * Проставляет время окончания для последнего события, которое влияет на отработанное время, текущего дня.
	 */
	public void stopLastActiveEvent() {
		Event lastWorkEvent = workDay.getLastWorkEvent();
		if (!workDay.getEvents().isEmpty() && lastWorkEvent.getState().equals(NOT_END)) {
			lastWorkEvent.setEndTime(LocalDateTime.now());
			lastWorkEvent.setState(END);
			em.merge(lastWorkEvent);
		}
	}

	public void removeEvent(Event removingEvent) {
		workDay.removeEvent(removingEvent);
		em.merge(workDay);
		em.remove(removingEvent);
	}

	/**
	 * Проставляет информацию о начале рабочего дня, в качестве времени прихода на работу проставляется текущее время.
	 */
	private void startWork() {
		workDay.setComingTime(workDay.getLastWorkEvent().getStartTime());
		workDay.setState(WorkDayState.WORKING);
	}

	/**
	 * Проставляет информацию об окончании рабочего дня, в качестве времени окончания проставляется текущее время. Так
	 * же создается {@linkplain Event событие}, которое характерезует собой интервал рабочего времени.
	 */
	// FIXME: Перед окончанием рабочего дня надо проверять, нет ли уже события влияющего на отработанное время, с таким
	// временным интервалом
	public void endWork() {
		stopLastActiveEvent();
		workDay.setOutTime(workDay.getLastWorkEvent().getEndTime());
		workDay.setState(WorkDayState.WORKED);
		em.merge(workDay);
	}

	/**
	 * Возвращает время прихода на работу для текущего рабочего дня. Если рабочий день не начался возвращает шаблон для
	 * пустого времени "__:__".
	 *
	 * @return Время прихода на работу для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	public String getComingTimeValue() {
		return workDay != null && !workDay.isNoWork() ? toHHMM(workDay.getComingTime()) : "__:__";
	}

	/**
	 * Возвращает время ухода с работы для текущего рабочего дня.
	 * <ul>
	 * <li>Если рабочий день не начался, то возвращает шаблон для пустого времени "__:__"</li>
	 * <li>Если рабочий день не закончен, то возвращает {@linkplain #getPossibleOutTimeValue() возможное время ухода}
	 * </li>
	 * <li>Если рабочий день окончен, то возвращает {@linkplain #getRealOutTimeValue() реальное время ухода}</li>
	 * </ul>
	 *
	 * @return Время ухода с работы для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	public String getOutTimeValue() {
		if (workDay != null && !workDay.isNoWork()) {
			return workDay.isWorked() ? getRealOutTimeValue() : getPossibleOutTimeValue();
		} else {
			return "__:__";
		}
	}

	/**
	 * Формирует заголовок для блока управления текущим рабочим днем. Заголовок формируется из даты в формате 'dd EE.' +
	 * состояние рабочего дня.
	 *
	 * @return Заголовок для блока управления текущим рабочим днем.
	 */
	public String getLegendValue() {
		return workDay != null ? toDDEE(workDay.getDay().getDay()) + " - " + workDay.getState().getDesc()
				: "Не найдено";
	}

	public String getTimeInfo() {
		return String.format("Начало: %s, %s %s", getComingTimeValue(), workDay.isWorked() ? "окончание" : "можно уйти",
				getOutTimeValue());
	}

	// *****************************************************************************************************************
	// Privates methods
	// *****************************************************************************************************************

	private void cleanAfterCreation() {
		selectedEventType = null;
	}

	/**
	 * Инициализирует информацию о текущем рабочем дне, если ее нет.
	 */
	private void initCurrentWorkDay() {
		workDay = wdDAO.getWorkDay(LocalDate.now(), SessionUtil.getWorker());
	}

	private void refreshEvent(Event event) {
		Event oldEventValue = em.find(Event.class, event.getId());
		event.setStartTime(oldEventValue.getStartTime());
		event.setEndTime(oldEventValue.getEndTime());
	}

	/**
	 * Из за того, что Primefaces проставляет 1970г если использовать компонент для ввода только времени необходимо
	 * формировать корректное значение времени.
	 *
	 * @param time
	 *            корректное время.
	 * @param day
	 *            день года.
	 * @return Корректное время с корректной датой.
	 */
	private LocalDateTime fixDate(LocalDateTime time, LocalDate day) {
		return LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), time.getHour(), time.getMinute());
	}

	/**
	 * @return Реальное время ухода для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	private String getRealOutTimeValue() {
		return toHHMM(workDay.getOutTime());
	}

	/**
	 * Вычисляет возможное время ухода для текущего рабочего дня, как сумму {@linkplain WorkDay#getComingTime() время
	 * прихода} + {@linkplain org.onbrains.entity.workDay.DayType#getWorkTimeInSecond() время которое надо отработать}.
	 *
	 * @return Возможное время ухода в милисекундах.
	 */
	private String getPossibleOutTimeValue() {
		return workDay != null && !workDay.isNoWork()
				? toHHMM(workDay.getComingTime().plusSeconds(workDay.getIdealWorkedTime())) : "__:__";
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

	public WorkDay getWorkDay() {
		return workDay;
	}

	public EventType getSelectedEventType() {
		return selectedEventType;
	}

	public void setSelectedEventType(EventType selectedEventType) {
		this.selectedEventType = selectedEventType;
	}

	public List<EventType> getEventTypes() {
		return etDAO.getEventTypes(true);
	}

	public int getMinHourForEndEvent(Event event) {
		return event.getStartTime().get(ChronoField.HOUR_OF_DAY);
	}

}