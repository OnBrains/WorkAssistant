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
import javax.enterprise.context.ConversationScoped;
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
@ConversationScoped
@Transactional
public class WorkDayFrameModel implements Serializable {

	@PersistenceContext
	private EntityManager em;
	@Inject
	private WorkDayDAOInterface wdDAO;
	@Inject
	private EventTypeDAOInterface etDAO;

	private WorkDay workDay;
	private EventType selectedEventType;

	@PostConstruct
	public void postConstruct() {
		if (workDay == null) {
			initWorkDay();
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
				cancelChangeEvent(editionEvent);
				Notification.warn("Невозможно сохранить изменения", "Время окончания события больше времени начала");
			} else {
				em.merge(editionEvent);
			}
		} else {
			cancelChangeEvent(editionEvent);
			Notification.warn("Невозможно сохранить изменения", "Пересечение временых интервалов у событий");
		}
		getCurrentWorkedTime();
	}

	private void cancelChangeEvent(Event editionEvent) {
		if (editionEvent != null) {
			Event oldEventValue = em.find(Event.class, editionEvent.getId());
			editionEvent.setStartTime(oldEventValue.getStartTime());
			editionEvent.setEndTime(oldEventValue.getEndTime());
		}
	}

	/**
	 * Проверяет можно ли закончить {@linkplain WorkDay#lastEvent последнее событие}.
	 *
	 * @return <strong>true</strong> - если можно.
	 */
	public boolean canStopEvent() {
		return !workDay.getEvents().isEmpty() && workDay.getLastWorkEvent().getState().equals(NOT_END);
	}

	/**
	 * {@linkplain #addNewEvent Создает событие} и отмечает начало рабочего дня если это первое событие для текущего
	 * дня. Если в момент создания нового события, {@linkplain WorkDay#lastEvent последнее событие} не закончено, то
	 * перед создание нового события, для него проставится статус "Закончено".
	 */
	public void startEvent() {
		// FIXME: заканчивать событие надо если создается событие влияющее на рабочее время
		if (!workDay.getEvents().isEmpty() && workDay.getLastWorkEvent().getState().equals(NOT_END)) {
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
		Event editingEvent = workDay.getLastWorkEvent();
		editingEvent.setEndTime(Calendar.getInstance());
		editingEvent.setState(END);
		em.merge(editingEvent);
	}

	// FIXME: разобраться почему не работает каскадное изменение. Должно происходить удаление события из БД при удалении
	// его из коллекции.
	public void removeEvent(Event removingEvent) {
		workDay.getEvents().remove(removingEvent);
		em.merge(workDay);
		em.remove(em.merge(removingEvent));
	}

	/**
	 * Проставляет информацию о начале рабочего дня, в качестве времени прихода на работу проставляется текущее время.
	 */
	// FIXME: Перед началом рабочего дня надо проверять, нет ли уже события влияющего на отработанное время, с таким
	// временным интервалом
	private void startWork() {
		workDay.setComingTime(workDay.getLastWorkEvent().getStartTime());
		workDay.setState(WorkDayState.WORKING);
		em.merge(workDay);
	}

	/**
	 * Проставляет информацию об окончании рабочего дня, в качестве времени окончания проставляется текущее время. Так
	 * же создается {@linkplain Event событие}, которое характерезует собой интервал рабочего времени.
	 */
	// FIXME: Перед окончанием рабочего дня надо проверять, нет ли уже события влияющего на отработанное время, с таким
	// временным интервалом
	public void endWork() {
		stopEvent();
		workDay.setOutTime(workDay.getLastWorkEvent().getEndTime());
		workDay.setState(WorkDayState.WORKED);
		em.merge(workDay);
	}

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "Не работал".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "Не работал".
	 */
	public boolean isNoWork() {
		return workDay != null && workDay.getState().equals(WorkDayState.NO_WORK);
	}

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "На работе".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "На работе".
	 */
	public boolean isWorking() {
		return workDay != null && workDay.getState().equals(WorkDayState.WORKING);
	}

	/**
	 * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "Отработал".
	 *
	 * @return <strong>true</strong> - если состояние текущего дня "Отработал".
	 */
	public boolean isWorked() {
		return workDay != null && workDay.getState().equals(WorkDayState.WORKED);
	}

	/**
	 * Возвращает время прихода на работу для текущего рабочего дня. Если рабочий день не начался возвращает шаблон для
	 * пустого времени "__:__".
	 *
	 * @return Время прихода на работу для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	public String getComingTime() {
		return workDay != null && !isNoWork()
				? DateFormatService.toHHMM(workDay.getComingTime().getTime()) : "__:__";
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
		if (workDay != null && !isNoWork()) {
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
	public Long getCurrentWorkedTime() {
		long workedTime = workDay.getSummaryWorkedTime();
		switch (workDay.getState()) {
		case WORKED:
			return workedTime;
		case WORKING:
			Event lastWorkEvent = workDay.getLastWorkEvent();
			if (lastWorkEvent != null && !lastWorkEvent.getState().equals(END)) {
				return getCurrentTimeInMSecond() - lastWorkEvent.getStartTime().getTimeInMillis() + workedTime;
			} else {
				return workedTime;
			}
		default:
			return 0L;
		}
	}

	// *****************************************************************************************************************
	// day statistic
	// *****************************************************************************************************************

	public boolean isRealWorkedTimeMoreIdeal() {
		return getCurrentWorkedTime() > workDay.getDay().getType().getWorkTimeInMSecond();
	}

	public long getWorkedTime() {
		return isRealWorkedTimeMoreIdeal() ? workDay.getDay().getType().getWorkTimeInMSecond()
				: getCurrentWorkedTime();
	}

	public long getDeltaTime() {
		long idealWorkedTime = workDay.getDay().getType().getWorkTimeInMSecond();
		return isRealWorkedTimeMoreIdeal() ? getCurrentWorkedTime() - idealWorkedTime
				: idealWorkedTime - getCurrentWorkedTime();
	}

	public String getStyleForWorkedTime() {
		String display = getWorkedTime() == 0 ? "none" : "table-cell";
		return "background-color: #4da9f1;padding: 4px; width: " + getPercentage(getSummaryTime(), getWorkedTime())
				+ "%; display: " + display + ";";
	}

	public String getStyleForDeltaTime() {
		String color = isRealWorkedTimeMoreIdeal() ? "green" : "chocolate";
		String display = getDeltaTime() == 0 ? "none" : "table-cell";
		return "background-color: " + color + "; padding: 4px;width: " + getPercentage(getSummaryTime(), getDeltaTime())
				+ "%; display: " + display + ";";
	}

	private float getPercentage(Long fullTime, Long partTime) {
		return (float) partTime * 100 / fullTime;
	}

	private long getSummaryTime() {
		return getWorkedTime() + getDeltaTime();
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
	public void initWorkDay() {
		workDay = wdDAO.getCurrentDayInfo(new Date(), SessionUtil.getWorker());
	}

    public void initWorkDay(Date selectedDate) {
        workDay = wdDAO.getCurrentDayInfo(selectedDate, SessionUtil.getWorker());
    }

	private boolean isPossibleEventTimeInterval(Calendar startTime, Calendar endTime) {
		return workDay.isPossibleTimeBoundaryForEvent(startTime, endTime);
	}

	private void updateWorkDayTime(Calendar comingTime, Calendar outTime) {
		boolean isChange = false;
		if (comingTime != null && workDay.needChangeComingTimeTo(comingTime)) {
			workDay.setComingTime(comingTime);
			isChange = true;
		}
		if (outTime != null && workDay.needChangeOutTimeTo(outTime)) {
			workDay.setOutTime(comingTime);
			isChange = true;
		}
		if (isChange) {
			em.merge(workDay);
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
		Event workEvent = new Event(workDay.getDay().getDay(), selectedEventType, selectedEventType.getTitle(),
				Calendar.getInstance());
		em.persist(workEvent);
		workDay.addEvent(workEvent);
		em.merge(workDay);
		return workEvent;
	}

	/**
	 * @return Реальное время ухода для текущего рабочего дня в формате <strong>HH:MM</strong>.
	 */
	private String getRealOutTime() {
		return DateFormatService.toHHMM(workDay.getOutTime().getTime());
	}

	/**
	 * Вычисляет возможное время ухода для текущего рабочего дня, как сумму {@linkplain WorkDay#getComingTime() время
	 * прихода} + {@linkplain org.onbrains.entity.workDay.DayType#getWorkTimeInMSecond() время которое надо отработать}.
	 *
	 * @return Возможное время ухода в милисекундах.
	 */
	private Long getPossibleOutTimeInMSecond() {
		return workDay != null && !isNoWork() ? workDay.getComingTime().getTimeInMillis()
				+ workDay.getDay().getType().getWorkTimeInMSecond() : 0L;
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
		return workDay != null ? DateFormatService.toHHMM(possibleOutComeTime.getTime()) : "__:__";
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

	private Long getCurrentTimeInMSecond() {
		return Calendar.getInstance().getTimeInMillis();
	}

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
		return event.getStartTime().get(Calendar.HOUR_OF_DAY);
	}

}
