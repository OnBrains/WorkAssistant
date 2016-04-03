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
public class CurrentDayFrameModel implements Serializable {

	@Inject
	private EntityManagerUtils em;
	@Inject
	private WorkDayDAOInterface wdDAO;
	@Inject
	private EventTypeDAOInterface etDAO;

	private WorkDay currentWorkDay;
	private EventType selectedEventType;

	@PostConstruct
	public void postConstruct() {
		if (currentWorkDay == null) {
			initCurrentWorkDay();
		}
	}

	public List<StatisticValue> getWorkDayStatistic() {
		List<StatisticValue> workDayStatistic = new LinkedList<>();
		if (currentWorkDay != null) {
			if (!currentWorkDay.isNoWork()) {
				long workedTime = currentWorkDay.isWorkingRequiredTime()
						? currentWorkDay.getDay().getType().getWorkTimeInSecond() : currentWorkDay.getWorkingTime();
				workDayStatistic.add(new StatisticValue(workedTime, "Отработано", "#4da9f1"));
			}
			workDayStatistic.add(currentWorkDay.isWorkingRequiredTime()
					? new StatisticValue(currentWorkDay.getDeltaTime(), "Переработок", "green")
					: new StatisticValue(currentWorkDay.getDeltaTime(), "Осталось", "chocolate"));
		}
		return workDayStatistic;
	}

	public void onRowEdit(RowEditEvent event) {
		Event editionEvent = (Event) event.getObject();
		LocalDateTime startTime = formationCorrectTime(editionEvent.getStartTime(), editionEvent.getDay());
		LocalDateTime endTime = formationCorrectTime(editionEvent.getEndTime(), editionEvent.getDay());
		if (currentWorkDay.isPossibleTimeBoundaryForEvent(startTime, endTime)) {
			editionEvent.setStartTime(startTime);
			editionEvent.setEndTime(endTime);
			currentWorkDay.changeTimeBy(editionEvent);
			em.merge(currentWorkDay);
			if (editionEvent.getState().equals(END)
					&& editionEvent.getEndTime().isBefore(editionEvent.getStartTime())) {
				Notification.warn("Невозможно сохранить изменения", "Время окончания события больше времени начала");
			} else {
				em.merge(editionEvent);
			}
		} else {
			Notification.warn("Невозможно сохранить изменения", "Пересечение временых интервалов у событий");
		}
	}

	/**
	 * Проверяет можно ли закончить {@linkplain WorkDay#getLastWorkEvent последнее событие}.
	 *
	 * @return <strong>true</strong> - если можно.
	 */
	public boolean canStopEvent() {
		return currentWorkDay != null && !currentWorkDay.getEvents().isEmpty()
				&& currentWorkDay.getLastWorkEvent().getState().equals(NOT_END);
	}

	/**
	 * Создает событие и отмечает начало рабочего дня если это первое событие для текущего дня. Если в момент создания
	 * нового события, {@linkplain WorkDay#getLastWorkEvent последнее событие} не закончено, то перед создание нового
	 * события, для него проставится статус "Закончено".
	 */
	public void startEvent() {
		stopLastActiveEvent();
		Event creationEvent = new Event(currentWorkDay.getDay().getDay(), selectedEventType,
				selectedEventType.getTitle(), LocalDateTime.now());
		String addEventMessage = currentWorkDay.addEvent(creationEvent);
		if (Objects.equals(addEventMessage, "")) {
			em.persist(creationEvent);
			if (currentWorkDay.isNoWork()
					&& !creationEvent.getType().getCategory().equals(EventCategory.NOT_INFLUENCE_ON_WORKED_TIME)) {
				startWork();
			}
			em.merge(currentWorkDay);
		} else {
			Notification.warn("Невозможно создать событие", addEventMessage);
		}
		cleanAfterCreation();
	}

	/**
	 * Проставляет время окончания для последнего события, которое влияет на отработанное время, текущего дня.
	 */
	// FIXME: заканчивать событие надо если создается событие влияющее на рабочее время
	public void stopLastActiveEvent() {
		Event lastWorkEvent = currentWorkDay.getLastWorkEvent();
		if (!currentWorkDay.getEvents().isEmpty() && lastWorkEvent.getState().equals(NOT_END)) {
			lastWorkEvent.setEndTime(LocalDateTime.now());
			lastWorkEvent.setState(END);
			em.merge(lastWorkEvent);
		}
	}

	// FIXME: разобраться почему не работает каскадное изменение. Должно происходить удаление события из БД при удалении
	// его из коллекции.
	public void removeEvent(Event removingEvent) {
		currentWorkDay.getEvents().remove(removingEvent);
		em.merge(currentWorkDay);
		em.remove(em.merge(removingEvent));
	}

	/**
	 * Проставляет информацию о начале рабочего дня, в качестве времени прихода на работу проставляется текущее время.
	 */
	// FIXME: Перед началом рабочего дня надо проверять, нет ли уже события влияющего на отработанное время, с таким
	// временным интервалом
	private void startWork() {
		currentWorkDay.setComingTime(currentWorkDay.getLastWorkEvent().getStartTime());
		currentWorkDay.setState(WorkDayState.WORKING);
	}

	/**
	 * Проставляет информацию об окончании рабочего дня, в качестве времени окончания проставляется текущее время. Так
	 * же создается {@linkplain Event событие}, которое характерезует собой интервал рабочего времени.
	 */
	// FIXME: Перед окончанием рабочего дня надо проверять, нет ли уже события влияющего на отработанное время, с таким
	// временным интервалом
	public void endWork() {
		stopLastActiveEvent();
		currentWorkDay.setOutTime(currentWorkDay.getLastWorkEvent().getEndTime());
		currentWorkDay.setState(WorkDayState.WORKED);
		em.merge(currentWorkDay);
	}

	/**
	 * Возвращает время прихода на работу для текущего рабочего дня. Если рабочий день не начался возвращает шаблон для
	 * пустого времени "__:__".
	 *
	 * @return Время прихода на работу для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	public String getComingTime() {
		return currentWorkDay != null && !currentWorkDay.isNoWork() ? toHHMM(currentWorkDay.getComingTime()) : "__:__";
	}

	/**
	 * Возвращает время ухода с работы для текущего рабочего дня.
	 * <ul>
	 * <li>Если рабочий день не начался, то возвращает шаблон для пустого времени "__:__"</li>
	 * <li>Если рабочий день не закончен, то возвращает {@linkplain #getPossibleOutTime() возможное время ухода}</li>
	 * <li>Если рабочий день окончен, то возвращает {@linkplain #getRealOutTime() реальное время ухода}</li>
	 * </ul>
	 *
	 * @return Время ухода с работы для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	public String getOutTime() {
		if (currentWorkDay != null && !currentWorkDay.isNoWork()) {
			return currentWorkDay.isWorked() ? getRealOutTime() : getPossibleOutTime();
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
		return currentWorkDay != null
				? toDDEE(currentWorkDay.getDay().getDay()) + " - " + currentWorkDay.getState().getDesc() : "Не найдено";
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
		currentWorkDay = wdDAO.getWorkDay(LocalDate.now(), SessionUtil.getWorker());
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
	private LocalDateTime formationCorrectTime(LocalDateTime time, LocalDate day) {
		return LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), time.getHour(), time.getMinute());
	}

	/**
	 * @return Реальное время ухода для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	private String getRealOutTime() {
		return toHHMM(currentWorkDay.getOutTime());
	}

	/**
	 * Вычисляет возможное время ухода для текущего рабочего дня, как сумму {@linkplain WorkDay#getComingTime() время
	 * прихода} + {@linkplain org.onbrains.entity.workDay.DayType#getWorkTimeInSecond() время которое надо отработать}.
	 *
	 * @return Возможное время ухода в милисекундах.
	 */
	private String getPossibleOutTime() {
		return currentWorkDay != null && !currentWorkDay.isNoWork()
				? toHHMM(currentWorkDay.getComingTime().plusSeconds(currentWorkDay.getIdealWorkedTime())) : "__:__";
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

	public WorkDay getCurrentWorkDay() {
		return currentWorkDay;
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