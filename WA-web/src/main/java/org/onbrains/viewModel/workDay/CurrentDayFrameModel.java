package org.onbrains.viewModel.workDay;

import org.onbrains.dao.DAOHelper;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.event.EventType;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.parsing.DateFormatService;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Naumov Oleg on 04.08.2015 21:40.
 */
@ManagedBean
@ViewScoped
public class CurrentDayFrameModel implements Serializable {

    @Inject
    private WorkDayDAOInterface wdDAO;
    @Inject
    private DAOHelper hDAO;

    private WorkDay currentWorkDay;

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
     * Проставляет информацию о начале рабочего дня, в качестве времени прихода на работу
     * проставляется текущее время.
     */
    public void startWork() {
        currentWorkDay.setComingTime(Calendar.getInstance());
        currentWorkDay.setState(WorkDayState.WORKING);
        wdDAO.update(currentWorkDay);
    }

    /**
     * Проставляет информацию об окончании рабочего дня, в качестве времени окончания
     * проставляется текущее время.
     * Так же создается {@linkplain Event событие}, которое характерезует собой интервал рабочего времени.
     */
    public void endWork() {
        currentWorkDay.setOutTime(Calendar.getInstance());
        currentWorkDay.setState(WorkDayState.WORKED);
        wdDAO.update(currentWorkDay);
        Event workEvent = new Event(currentWorkDay.getDay().getDay(), (EventType) hDAO.find(EventType.class, EventType.WORK_EVENT_TYPE_ID),
                "Работа за " + DateFormatService.toYYYYMMDD(new Date()), currentWorkDay.getComingTime(), currentWorkDay.getOutTime());
        hDAO.persist(workEvent);
    }

    /**
     * Проверяет находится ли текущий день в {@linkplain WorkDayState состоянии} "Не работал".
     *
     * @return <strong>true</strong> - если состояние текущего дня "Не работал".
     */
    private boolean isNoWork() {
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
     * Возвращает время прихода на работу для текущего рабочего дня.
     * Если рабочий день не начался возвращает шаблон для пустого времени "__:__".
     *
     * @return Время прихода на работу для текущего рабочего дня в формате <strong>HH:MM</strong>.
     */
    public String getComingTime() {
        return currentWorkDay != null && !isNoWork() ? DateFormatService.toHHMM(currentWorkDay.getComingTime().getTime()) : "__:__";
    }

    /**
     * Возвращает время ухода с работы для текущего рабочего дня.
     * <ul>
     *     <li>Если рабочий день не начался, то возвращает шаблон для пустого времени "__:__"</li>
     *     <li>Если рабочий день не закончин, то возвращает {@linkplain #getPossibleOutTime() возможное время ухода}</li>
     *     <li>Если рабочий день окончен, то возвращает {@linkplain #getRealOutTime() реальное время ухода}</li>
     * </ul>
     *
     * @return Время ухода с работы для текущего рабочего дня в формате <strong>HH:MM</strong>.
     */
    public String getOutTime() {
        if (currentWorkDay != null && isWorking()) {
            if (isWorked()) {
                return getRealOutTime();
            } else {
                return getPossibleOutTime();
            }
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
     * Вычисляет возможное время ухода для текущего рабочего дня,
     * как сумму {@linkplain WorkDay#getComingTime() время прихода} + {@linkplain org.onbrains.entity.workDay.DayType#getWorkTimeInMSecond() время которое надо отработать}.
     *
     * @return Возможное время ухода в милисекундах.
     */
    private Long getPossibleOutTimeInMSecond() {
        return currentWorkDay != null ? currentWorkDay.getComingTime().getTimeInMillis() +
                currentWorkDay.getDay().getType().getWorkTimeInMSecond() : 0;
    }

    /**
     * Вычисляет возможное время ухода, в зависимости от времени прихода.
     * Актуально для случая, когда рабочий день не закончин.
     *
     * @return - время прихода + время в зависимости от типа дня, если день не найден, то "__:__"
     */
    private String getPossibleOutTime() {
        Calendar possibleOutComeTime = Calendar.getInstance();
        possibleOutComeTime.setTimeInMillis(getPossibleOutTimeInMSecond());
        return currentWorkDay != null ? DateFormatService.toHHMM(possibleOutComeTime.getTime()) : "__:__";
    }

    /**
     * Simple getters and setters
     */

    public WorkDay getCurrentWorkDay() {
        return currentWorkDay;
    }

}
