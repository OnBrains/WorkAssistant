package org.onbrains.viewModel.workDay;

import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.dao.workDayEvent.EventDAOInterface;
import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
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
    private SessionUtil sessionUtil;
    @Inject
    private WorkDayDAOInterface wdDAO;
    @Inject
    private EventTypeDAOInterface etDAO;
    @Inject
    private EventDAOInterface eDAO;

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
        currentWorkDay = wdDAO.getCurrentDayInfo(new Date(), sessionUtil.getWorker());
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
        Event workEvent = new Event(currentWorkDay.getDay().getDay(), etDAO.find(EventType.WORK_EVENT_TYPE_ID),
                "Работа за " + DateFormatService.toYYYYMMDD(new Date()), currentWorkDay.getComingTime(), currentWorkDay.getOutTime());
        eDAO.create(workEvent);
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
     * Simple getters and setters
     */

    public WorkDay getCurrentWorkDay() {
        return currentWorkDay;
    }

}
