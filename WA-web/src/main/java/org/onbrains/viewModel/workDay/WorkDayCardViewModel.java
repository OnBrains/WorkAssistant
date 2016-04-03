package org.onbrains.viewModel.workDay;

import org.onbrains.component.statistic.StatisticValue;
import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.utils.information.Notification;
import org.primefaces.event.RowEditEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.onbrains.entity.event.EventState.END;

/**
 * @author Naumov Oleg on 09.10.2015 21:42.
 */
@Named
@ConversationScoped
public class WorkDayCardViewModel implements Serializable {

    @Inject
    private EntityManagerUtils em;

    private WorkDay selectedWorkDay;

    @PostConstruct
    public void postConstruct() {
        selectedWorkDay = initSelectedWorkDay();
    }

    public WorkDay initSelectedWorkDay() {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("workDay");
        return path == null ? null : em.find(WorkDay.class, Long.parseLong(path.split("-")[1]));
    }

    public List<StatisticValue> getWorkDayStatistic() {
        List<StatisticValue> workDayStatistic = new LinkedList<>();
        if (selectedWorkDay != null) {
            if (!selectedWorkDay.isNoWork()) {
                long workedTime = selectedWorkDay.isWorkingRequiredTime()
                        ? selectedWorkDay.getDay().getType().getWorkTimeInSecond() : selectedWorkDay.getWorkingTime();
                workDayStatistic.add(new StatisticValue(workedTime, "Отработано", "#4da9f1"));
            }
            workDayStatistic.add(selectedWorkDay.isWorkingRequiredTime()
                    ? new StatisticValue(selectedWorkDay.getDeltaTime(), "Переработок", "green")
                    : new StatisticValue(selectedWorkDay.getDeltaTime(), "Осталось", "chocolate"));
        }
        return workDayStatistic;
    }

    public void onRowEdit(RowEditEvent event) {
        Event editionEvent = (Event) event.getObject();
        LocalDateTime startTime = formationCorrectTime(editionEvent.getStartTime(), editionEvent.getDay());
        LocalDateTime endTime = formationCorrectTime(editionEvent.getEndTime(), editionEvent.getDay());
        if (selectedWorkDay.isPossibleTimeBoundaryForEvent(startTime, endTime)) {
            editionEvent.setStartTime(startTime);
            editionEvent.setEndTime(endTime);
            selectedWorkDay.changeTimeBy(editionEvent);
            em.merge(selectedWorkDay);
            if (editionEvent.getState().equals(END) && editionEvent.getEndTime().isBefore(editionEvent.getStartTime())) {
                Notification.warn("Невозможно сохранить изменения", "Время окончания события больше времени начала");
            } else {
                em.merge(editionEvent);
            }
        } else {
            Notification.warn("Невозможно сохранить изменения", "Пересечение временых интервалов у событий");
        }
    }

    public void removeEvent(Event removingEvent) {
        selectedWorkDay.getEvents().remove(removingEvent);
        em.remove(em.merge(removingEvent));
        em.merge(selectedWorkDay);
    }

    public int getMinHourForEndEvent(Event event) {
        return event.getStartTime().get(ChronoField.HOUR_OF_DAY);
    }

    private LocalDateTime formationCorrectTime(LocalDateTime time, LocalDate day) {
        time.withDayOfMonth(day.getDayOfMonth());
        return time;
    }

    public WorkDay getSelectedWorkDay() {
        return selectedWorkDay;
    }

}
