package org.onbrains.viewModel.workDay;

import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.event.EventCategory;
import org.onbrains.entity.event.EventType;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.information.Notification;
import org.onbrains.utils.parsing.DateFormatService;
import org.primefaces.event.RowEditEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.onbrains.entity.event.EventState.END;
import static org.onbrains.entity.event.EventState.NOT_END;

/**
 * @author Naumov Oleg on 04.08.2015 21:40.
 */
@Named
@SessionScoped
@Transactional
public class CurrentDayFrameModel implements Serializable {

	@PersistenceContext
	private EntityManager em;
	@Inject
	private WorkDayDAOInterface wdDAO;
	@Inject
	private EventTypeDAOInterface etDAO;

	private WorkDay currentWorkDay;
	private EventType selectedEventType;

	@PostConstruct
	public void postConstruct() {
		if (currentWorkDay == null) {
			initializationCurrentWorkDay();
		}
	}

	public void onRowEdit(RowEditEvent event) {
		Event editionEvent = (Event) event.getObject();
		Calendar startTime = formationCorrectTime(editionEvent.getStartTime(), editionEvent.getDay());
		Calendar endTime = formationCorrectTime(editionEvent.getEndTime(), editionEvent.getDay());
		if (isPossibleEventTimeInterval(startTime, endTime)) {
			updateWorkDayTime(startTime, endTime);
			editionEvent.setStartTime(startTime);
			editionEvent.setEndTime(endTime);
			if (editionEvent.getEndTime().getTimeInMillis() < editionEvent.getStartTime().getTimeInMillis()) {
                Notification.warn("Невозможно сохранить изменения", "Время окончания события больше времени начала");
			} else {
				em.merge(editionEvent);
			}
		} else {
			Notification.warn("Невозможно сохранить изменения", "Пересечение временых интервалов у событий");
		}
	}

	/**
	 * Проверяет можно ли закончить {@linkplain WorkDay#lastEvent последнее событие}.
	 *
	 * @return <strong>true</strong> - если можно.
	 */
	public boolean canStopEvent() {
		return !currentWorkDay.getEvents().isEmpty() && currentWorkDay.getLastWorkEvent().getState().equals(NOT_END);
	}

	/**
	 * {@linkplain #addNewEvent Создает событие} и отмечает начало рабочего дня если это первое событие для текущего
	 * дня. Если в момент создания нового события, {@linkplain WorkDay#lastEvent последнее событие} не закончено, то
	 * перед создание нового события, для него проставится статус "Закончено".
	 */
	public void startEvent() {
		if (!currentWorkDay.getEvents().isEmpty() && currentWorkDay.getLastWorkEvent().getState().equals(NOT_END)) {
			stopEvent();
		}
		Event creationEvent = addNewEvent();
		if (isNoWork() && !creationEvent.getType().getCategory().equals(EventCategory.NOT_INFLUENCE_ON_WORKED_TIME)) {
			startWork();
		}
		cleanAfterCreation();
	}

	/**
	 * Проставляет время окончания для последнего события текущего дня.
	 */
	public void stopEvent() {
		Event editingEvent = currentWorkDay.getLastWorkEvent();
		editingEvent.setEndTime(Calendar.getInstance());
		editingEvent.setState(END);
		em.merge(editingEvent);
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
		em.merge(currentWorkDay);
	}

	/**
	 * Проставляет информацию об окончании рабочего дня, в качестве времени окончания проставляется текущее время. Так
	 * же создается {@linkplain Event событие}, которое характерезует собой интервал рабочего времени.
	 */
	// FIXME: Перед окончанием рабочего дня надо проверять, нет ли уже события влияющего на отработанное время, с таким
	// временным интервалом
	public void endWork() {
		stopEvent();
		currentWorkDay.setOutTime(currentWorkDay.getLastWorkEvent().getEndTime());
		currentWorkDay.setState(WorkDayState.WORKED);
	}

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "Не работал".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "Не работал".
	 */
	public boolean isNoWork() {
		return currentWorkDay != null && currentWorkDay.getState().equals(WorkDayState.NO_WORK);
	}

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "На работе".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "На работе".
	 */
	public boolean isWorking() {
		return currentWorkDay != null && currentWorkDay.getState().equals(WorkDayState.WORKING);
	}

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "Отработал".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "Отработал".
	 */
	public boolean isWorked() {
		return currentWorkDay != null && currentWorkDay.getState().equals(WorkDayState.WORKED);
	}

	/**
	 * Возвращает время прихода на работу для текущего рабочего дня. Если рабочий день не начался возвращает шаблон для
	 * пустого времени "__:__".
	 *
	 * @return Время прихода на работу для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	public String getComingTime() {
		return currentWorkDay != null && !isNoWork()
				? DateFormatService.toHHMM(currentWorkDay.getComingTime().getTime()) : "__:__";
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
		if (currentWorkDay != null && !isNoWork()) {
			return isWorked() ? getRealOutTime() : getPossibleOutTime();
		} else {
			return "__:__";
		}
	}

	/**
	 * Вычисляет отработанное на текущий момент время. Вычисления изменяются в зависимостри от {@linkplain WorkDayState
	 * состояния текущего рабочего дня}:
	 * <ul>
	 * <li><strong>Рабочий день закончен</strong> - отработанное время = {@link WorkDay#getSummaryWorkedTime()}</li>
	 * <li><strong>Рабочий день еще идет(последнее рабочее событие закончено)</strong> - отработанное время =
	 * {@link WorkDay#getSummaryWorkedTime()}</li>
	 * <li><strong>Рабочий день еще идет(последнее рабочее событие не закончено)</strong> - отработанное время =
	 * {@link WorkDay#getSummaryWorkedTime()} + {@linkplain #getCurrentTimeInMSecond() текущее время} -
	 * {@linkplain WorkDay#getLastWorkEvent() время начала последнего события}, которое {@linkplain EventType влияет на
	 * отработанное время}.</li>
	 * <li><strong>Рабочий день не начат</strong> - шаблон "__:__"</li>
	 * </ul>
	 * 
	 * @return Отработанное на текущий момент время, если рабочий день не начет, то "__:__".
	 */
	// FIXME: вероятно код для состояния в работе излишен, так как тоже самое вычисляется в времени события.
	public String getCurrentWorkedTime() {
		long workedTime = currentWorkDay.getSummaryWorkedTime();
		switch (currentWorkDay.getState()) {
		case WORKED:
			return DateFormatService.mSecToHHMM(workedTime);
		case WORKING:
			Event lastWorkEvent = currentWorkDay.getLastWorkEvent();
			if (lastWorkEvent != null && !lastWorkEvent.getState().equals(END)) {
				long currentWorkedTime = getCurrentTimeInMSecond() - lastWorkEvent.getStartTime().getTimeInMillis()
						+ workedTime;
				return DateFormatService.mSecToHHMM(currentWorkedTime);
			} else {
				return DateFormatService.mSecToHHMM(workedTime);
			}
		default:
			return "__:__";
		}
	}

	/**
	 * Определяет больше ли текущее время, чем возможное {@linkplain #getPossibleOutTimeInMSecond() возможное время
	 * ухода}. Если да, то получается что идут переработки, в противном случае еще недоработки.
	 *
	 * @return <strong>true</strong> - если текущее время больше возможного времени ухода.
	 */
	@Deprecated
	public boolean currentTimeMorePossibleOutTime() {
		return currentWorkDay != null && getCurrentTimeInMSecond() > getPossibleOutTimeInMSecond();
	}

	@Deprecated
	public String getLeftTime() {
		Long leftTime = Math.abs(getPossibleOutTimeInMSecond() - getCurrentTimeInMSecond());
		return !isNoWork() ? DateFormatService.mSecToHHMM(leftTime) : "__:__";
	}

	/**
	 * Формирует заголовок для блока управления текущим рабочим днем. Заголовок формируется из даты в формате 'dd EE.' +
	 * состояние рабочего дня.
	 *
	 * @return Заголовок для блока управления текущим рабочим днем.
	 */
	public String getLegendValue() {
		return currentWorkDay != null ? DateFormatService.toDDEE(currentWorkDay.getDay().getDay()) + " - "
				+ currentWorkDay.getState().getDesc() : "Не найдено";
	}

	// *****************************************************************************************************************
	// Block with privates methods
	// *****************************************************************************************************************

	private void cleanAfterCreation() {
		selectedEventType = null;
	}

	/**
	 * Инициализирует информацию о текущем рабочем дне, если ее нет.
	 */
	private void initializationCurrentWorkDay() {
		currentWorkDay = wdDAO.getCurrentDayInfo(new Date(), SessionUtil.getWorker());
	}

	private boolean isPossibleEventTimeInterval(Calendar startTime, Calendar endTime) {
		return currentWorkDay.isPossibleTimeBoundaryForEvent(startTime, endTime);
	}

	private void updateWorkDayTime(Calendar comingTime, Calendar outTime) {
		boolean isChange = false;
		if (comingTime != null && currentWorkDay.needChangeComingTimeTo(comingTime)) {
			currentWorkDay.setComingTime(comingTime);
			isChange = true;
		}
		if (outTime != null && currentWorkDay.needChangeOutTimeTo(outTime)) {
			currentWorkDay.setOutTime(comingTime);
			isChange = true;
		}
		if (isChange) {
			em.merge(currentWorkDay);
		}
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
	private Calendar formationCorrectTime(Calendar time, Date day) {
		Calendar correctTime = Calendar.getInstance();
		correctTime.setTime(day);
		correctTime.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
		correctTime.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
		return correctTime;
	}

	/**
	 * Создает новое событие и добавляет его к списку событий текущего дня.
	 *
	 * @return Созданное событие.
	 */
	private Event addNewEvent() {
		Event workEvent = new Event(currentWorkDay.getDay().getDay(), selectedEventType, selectedEventType.getTitle(),
				Calendar.getInstance());
		em.persist(workEvent);
		currentWorkDay.addEvent(workEvent);
		em.merge(currentWorkDay);
		return workEvent;
	}

	/**
	 * @return Реальное время ухода для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	private String getRealOutTime() {
		return DateFormatService.toHHMM(currentWorkDay.getOutTime().getTime());
	}

	/**
	 * Вычисляет возможное время ухода для текущего рабочего дня, как сумму {@linkplain WorkDay#getComingTime() время
	 * прихода} + {@linkplain org.onbrains.entity.workDay.DayType#getWorkTimeInMSecond() время которое надо отработать}.
	 *
	 * @return Возможное время ухода в милисекундах.
	 */
	private Long getPossibleOutTimeInMSecond() {
		return currentWorkDay != null && !isNoWork() ? currentWorkDay.getComingTime().getTimeInMillis()
				+ currentWorkDay.getDay().getType().getWorkTimeInMSecond() : 0L;
	}

	/**
	 * Вычисляет возможное время ухода, в зависимости от времени прихода. Актуально для случая, когда рабочий день не
	 * закончен.
	 *
	 * @return - Время прихода + время в зависимости от типа дня, если день не найден, то "__:__".
	 */
	private String getPossibleOutTime() {
		Calendar possibleOutComeTime = Calendar.getInstance();
		possibleOutComeTime.setTimeInMillis(getPossibleOutTimeInMSecond());
		return currentWorkDay != null ? DateFormatService.toHHMM(possibleOutComeTime.getTime()) : "__:__";
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

	private Long getCurrentTimeInMSecond() {
		return Calendar.getInstance().getTimeInMillis();
	}

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
		return event.getStartTime().get(Calendar.HOUR_OF_DAY);
	}

}
