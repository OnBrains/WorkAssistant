package org.onbrains.viewModel.workDay;

import org.onbrains.dao.DAOHelper;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.event.EventType;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.information.Notification;
import org.onbrains.utils.parsing.DateFormatService;
import org.primefaces.event.RowEditEvent;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 04.08.2015 21:40.
 */
@ManagedBean
@ViewScoped
public class CurrentDayFrameModel implements Serializable {

	@Inject
	private WorkDayDAOInterface wdDAO;
	@Inject
	private EventTypeDAOInterface etDAO;
	@Inject
	private DAOHelper hDAO;

	private WorkDay currentWorkDay;
	private EventType selectedEventType;

	@PostConstruct
	public void postConstruct() {
		if (currentWorkDay == null) {
			initializationCurrentWorkDay();
		}
	}

	/**
	 * Инициализирует информацию о текущем рабочем дне, если ее нет.
	 */
	private void initializationCurrentWorkDay() {
		currentWorkDay = wdDAO.getCurrentDayInfo(new Date(), SessionUtil.getWorker());
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

	public void onRowEdit(RowEditEvent event) {
		Event editionEvent = (Event) event.getObject();
		editionEvent.setStartTime(formationCorrectTime(editionEvent.getStartTime(), editionEvent.getDay()));
		editionEvent.setEndTime(formationCorrectTime(editionEvent.getEndTime(), editionEvent.getDay()));
		if (editionEvent.getEndTime().getTimeInMillis() < editionEvent.getStartTime().getTimeInMillis()) {
			Notification.warn("Невозможно сохранить изменения", "Время окончания события больше времени начала");
		} else {
			hDAO.merge(editionEvent);
		}
	}

	/**
	 * Создает новое событие и добавляет его к списку событий текущего дня.
	 *
	 * @return Созданное событие.
	 */
	private Event addNewEvent() {
		Event workEvent = new Event(currentWorkDay.getDay().getDay(), selectedEventType, selectedEventType.getTitle(),
				Calendar.getInstance());
		hDAO.persist(workEvent);
		currentWorkDay.getEvents().add(workEvent);
		wdDAO.update(currentWorkDay);
		return workEvent;
	}

	/**
	 * {@linkplain #addNewEvent Создает событие} и отмечает начало рабочего дня если это первое событие для текущего
	 * дня.
	 */
	public void startEvent() {
		addNewEvent();
		if (currentWorkDay.getEvents().isEmpty()) {
			startWork();
		}
	}

	/**
	 * Проставляет время окончания для последнего события текущего дня.
	 */
	public void stopEvent() {
		Event editingEvent = currentWorkDay.getLastEvent();
		editingEvent.setEndTime(Calendar.getInstance());
		hDAO.merge(editingEvent);
	}

	/**
	 * Проставляет информацию о начале рабочего дня, в качестве времени прихода на работу проставляется текущее время.
	 */
	// FIXME: Перед началом рабочего дня надо проверять, нет ли уже события влияющего на отработанное время, с таким
	// временным интервалом
	public void startWork() {
		currentWorkDay.setComingTime(currentWorkDay.getLastEvent().getStartTime());
		currentWorkDay.setState(WorkDayState.WORKING);
	}

	/**
	 * Проставляет информацию об окончании рабочего дня, в качестве времени окончания проставляется текущее время. Так
	 * же создается {@linkplain Event событие}, которое характерезует собой интервал рабочего времени.
	 */
	// FIXME: Перед окончанием рабочего дня надо проверять, нет ли уже события влияющего на отработанное время, с таким
	// временным интервалом
	public void endWork() {
		currentWorkDay.setOutTime(Calendar.getInstance());
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
	 * <li>Если рабочий день не закончин, то возвращает {@linkplain #getPossibleOutTime() возможное время ухода}</li>
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
		return currentWorkDay != null ? currentWorkDay.getComingTime().getTimeInMillis()
				+ currentWorkDay.getDay().getType().getWorkTimeInMSecond() : 0L;
	}

	/**
	 * Вычисляет возможное время ухода, в зависимости от времени прихода. Актуально для случая, когда рабочий день не
	 * закончин.
	 *
	 * @return - Время прихода + время в зависимости от типа дня, если день не найден, то "__:__".
	 */
	private String getPossibleOutTime() {
		Calendar possibleOutComeTime = Calendar.getInstance();
		possibleOutComeTime.setTimeInMillis(getPossibleOutTimeInMSecond());
		return currentWorkDay != null ? DateFormatService.toHHMM(possibleOutComeTime.getTime()) : "__:__";
	}

	/**
	 * Вычисляет отработанное на текущий момент время. Если рабочий день не закончин, то отработанное время вычисляется
	 * как текущие время минус {@linkplain WorkDay#getComingTime() время прихода}. Если рабочий день закончен, то
	 * берется {@linkplain WorkDay#getSummaryWorkedTime() суммарное отработанное время}.
	 *
	 * @return Отработанное на текущий момент время, если рабочий день не начет, то "__:__".
	 */
	public String getCurrentWorkedTime() {
		if (!isNoWork()) {
			Long currentWorkedTime = isWorked() ? currentWorkDay.getSummaryWorkedTime()
					: getCurrentTimeInMSecond() - currentWorkDay.getComingTime().getTimeInMillis();
			return DateFormatService.mSecToHHMM(currentWorkedTime);
		} else {
			return "__:__";
		}
	}

	/**
	 * Определяет больше ли текущее время, чем возможное {@linkplain #getPossibleOutTimeInMSecond() возможное время
	 * ухода}. Если да, то получается что идут переработки, в противном случае еще недоработки.
	 *
	 * @return <strong>true</strong> - если текущее время больше возможного времени ухода.
	 */
	public boolean currentTimeMorePossibleOutTime() {
		return currentWorkDay != null && getCurrentTimeInMSecond() > getPossibleOutTimeInMSecond();
	}

	// FIXME: написать комментарий
	public String getDeltaTime() {
		Long deltaTime = Math.abs(getPossibleOutTimeInMSecond() - getCurrentTimeInMSecond());
		return !isNoWork() ? DateFormatService.mSecToHHMM(deltaTime) : "__:__";
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

	/**
	 * Simple getters and setters
	 */

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

	public List<EventType> getAllEventType() {
		return etDAO.getAllEventType();
	}

	public Date getCurrentTime() {
		return Calendar.getInstance().getTime();
	}

	public int getMinHourForEndEvent(Event event) {
		return event.getStartTime().get(Calendar.HOUR_OF_DAY);
	}

}
