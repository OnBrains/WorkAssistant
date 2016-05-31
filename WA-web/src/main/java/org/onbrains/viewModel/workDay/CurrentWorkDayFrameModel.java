package org.onbrains.viewModel.workDay;

import static org.onbrains.entity.event.EventCategory.NOT_INFLUENCE_ON_WORKED_TIME;
import static org.onbrains.entity.event.EventState.END;
import static org.onbrains.entity.event.EventState.NOT_END;
import static org.onbrains.utils.parsing.DateFormatService.toDDEE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.event.EventType;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.information.Notification;

/**
 * @author Naumov Oleg on 04.08.2015 21:40.
 */
@Named
@ViewScoped
public class CurrentWorkDayFrameModel extends WorkDayFrameModel {

	private static final long serialVersionUID = 1715730076561263624L;

	@Inject
	private EntityManagerUtils em;
	@Inject
	private WorkDayDAOInterface wdDAO;
	@Inject
	private EventTypeDAOInterface etDAO;

	private List<EventType> possibleEventTypes;
	private EventType selectedEventType;

	@PostConstruct
	public void postConstruct() {
		if (workDay == null) {
			initCurrentWorkDay();
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
		Event creationEvent = new Event(workDay, selectedEventType, selectedEventType.getTitle(), LocalDateTime.now());
		String addEventMessage = workDay.addEvent(creationEvent);
		if (Objects.equals(addEventMessage, "")) {
			em.persist(creationEvent);
			if (workDay.isNoWork() && !creationEvent.getType().getCategory().equals(NOT_INFLUENCE_ON_WORKED_TIME)) {
				startWork();
			}
//			em.merge(workDay);
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
	public void endWork() {
		stopLastActiveEvent();
		workDay.setOutTime(workDay.getLastWorkEvent().getEndTime());
		workDay.setState(WorkDayState.WORKED);
		em.merge(workDay);
	}

	/**
	 * Формирует заголовок для блока управления текущим рабочим днем. Заголовок формируется из даты в формате 'dd EE.' +
	 * состояние рабочего дня.
	 *
	 * @return Заголовок для блока управления текущим рабочим днем.
	 */
	public String getLegendValue() {
		return workDay != null ? toDDEE(workDay.getDay().getDate()) + " - " + workDay.getState().getDesc()
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

	public List<EventType> getPossibleEventTypes() {
		if (possibleEventTypes == null) {
			possibleEventTypes = etDAO.getEventTypes(true).stream()
					.filter(eventType -> !eventType.getCategory().equals(NOT_INFLUENCE_ON_WORKED_TIME))
					.collect(Collectors.toList());
		}
		return possibleEventTypes;
	}

	public int getMinHourForEndEvent(Event event) {
		return event.getStartTime().get(ChronoField.HOUR_OF_DAY);
	}

}