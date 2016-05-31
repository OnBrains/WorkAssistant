package org.onbrains.viewModel.workDay;

import static org.onbrains.entity.event.EventState.END;
import static org.onbrains.utils.parsing.DateFormatService.fixDate;
import static org.onbrains.utils.parsing.DateFormatService.toHHMM;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.onbrains.component.statistic.StatisticValue;
import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.utils.information.Notification;
import org.primefaces.event.RowEditEvent;

/**
 * @author Naumov Oleg on 12.04.2016.
 */
@Named
@ViewScoped
public class WorkDayFrameModel implements Serializable {

	private static final long serialVersionUID = 3087806454501024801L;

	@Inject
	private EntityManagerUtils em;

	protected WorkDay workDay;

	public void setWorkDay(WorkDay workDay) {
		this.workDay = workDay;
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
			em.merge(editableEvent);
			em.merge(workDay);
		} else {
			refreshEvent(editableEvent);
			Notification.warn("Невозможно сохранить изменения", "Пересечение временых интервалов у событий");
		}
	}

    public void onWorkDayEdit() {
        em.merge(workDay);
    }

	public void removeEvent(Event removingEvent) {
//		em.remove(removingEvent);
//        em.flush();
        workDay.removeEvent(removingEvent);
        em.merge(workDay);
        em.remove(removingEvent);
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
		}
		return "__:__";
	}

	private void refreshEvent(Event event) {
		Event oldEventValue = em.find(Event.class, event.getId());
		event.setStartTime(oldEventValue.getStartTime());
		event.setEndTime(oldEventValue.getEndTime());
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

    public WorkDayState[] getWorkDayStates() {
        return WorkDayState.values();
    }

}