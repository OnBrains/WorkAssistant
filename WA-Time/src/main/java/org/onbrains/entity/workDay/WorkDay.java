package org.onbrains.entity.workDay;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.onbrains.entity.SuperClass;
import org.onbrains.entity.day.Day;
import org.onbrains.entity.workDayEvent.Event;
import org.onbrains.entity.worker.Worker;

/**
 * @author Naumov Oleg on 21.03.2015 21:20.
 */

@Entity
@Table(name = "WORK_DAY", uniqueConstraints = {@UniqueConstraint(columnNames = {"WORKER_ID", "DAY_ID"})})
@NamedQueries
    ({
        @NamedQuery(name = WorkDay.GET_WORK_DAYS_BY_MONTH,
                query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day.day, 'yyyyMM') = to_char(:month, 'yyyyMM') order by wt.day"),
        @NamedQuery(name = WorkDay.GET_CURRENT_DAY,
                query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day.day, 'yyyyMMdd') = to_char(:day, 'yyyyMMdd')"),
        @NamedQuery(name = WorkDay.GET_DAYS_PRIOR_CURRENT_DAY,
                query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day, 'yyyyMM') = to_char(:month, 'yyyyMM') and wt.day <= :month order by wt.day")
    })
public class WorkDay extends SuperClass {

    public static final String GET_WORK_DAYS_BY_MONTH = "WorkTimeDAO.getWorkDaysByMonth";
    public static final String GET_CURRENT_DAY = "WorkTimeDAO.getCurrentDay";
    public static final String GET_DAYS_PRIOR_CURRENT_DAY = "WorkTimeDAO.getDaysPriorCurrentDay";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKER_ID", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DAY_ID", nullable = false)
    private Day day;

    @Column(name = "COMING_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar comingTime;

    @Column(name = "OUT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar outTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", length = 16, nullable = false)
    private WorkDayState state = WorkDayState.NO_WORK;

    @ManyToMany(mappedBy = "workDays", fetch = FetchType.LAZY)
    private List<Event> events = new ArrayList<>();

    @Transient
    private Long summaryWorkedTime;

    @Transient
    private boolean workedFullDay;

    @Transient
    private Long deltaTime;

    protected WorkDay() {
    }

    public WorkDay(Worker worker, Day day) {
        this.worker = worker;
        this.day = day;
        //TODO: надо ли сетить состояние тут?
        this.state = WorkDayState.NO_WORK;
    }

    /**
     * @return Работник, к которому относится данный рабочий день.
     */
    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    /**
     * @return Конкретный день в году
     */
    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    /**
     * Время начала рабочего дня. Берётся минимальное время прихода из всех {@linkplain Event событий},
     * {@linkplain org.onbrains.entity.workDayEvent.EventType#isWorking() время которых идет в зачет отработанного}.
     *
     * @return Время начала рабочего дня.
     */
    public Calendar getComingTime() {
        return comingTime;
    }

    public void setComingTime(Calendar comingTime) {
        this.comingTime = comingTime;
    }

    /**
     * Время окончания рабочего дня. Берётся максимальное время ухода из всех {@linkplain Event событий},
     * {@linkplain org.onbrains.entity.workDayEvent.EventType#isWorking() время которых идет в зачет отработанного}
     *
     * @return Время окончания рабочего дня.
     */
    public Calendar getOutTime() {
        return outTime;
    }

    public void setOutTime(Calendar outTime) {
        this.outTime = outTime;
    }

    /**
     * Состаяние рабочего дня. Показывает в какой стадии находится рабочий день.
     * Состояние <strong>На работе</strong> может быть выбрано только для текущего рабочего дня.
     *
     * @return Состояние, в котором находится рабочий день.
     */
    public WorkDayState getState() {
        return state;
    }

    public void setState(WorkDayState state) {
        this.state = state;
    }

    /**
     * @return Перечень событий относящихся к рабочему дню.
     */
    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /**
     * Суммарное отработаное время в мил. секундах.
     * Равняется время ухода минус время прихода. Если рабочий день не закончен = 0.
     *
     * @return - разница между временем ухода и временем прихода
     */
    public Long getSummaryWorkedTime() {
        return getState().equals(WorkDayState.WORKED) ? outTime.getTimeInMillis() - comingTime.getTimeInMillis() : 0;
    }

    /**
     * Определяет отработано ли рабочий день полностью
     *
     * @return - true если отработан весь день
     */
    public boolean isWorkedFullDay() {
        return getSummaryWorkedTime() > day.getType().getWorkTimeInMSecond();
    }

    /**
     * Вычисляет переработанное или недоработанное время, для завершенного дня.
     *
     * @return - переработанное/недоработанное время в мил. секундах, если день не закончен вернет 0
     */
    public Long getDeltaTime() {
        if (getState().equals(WorkDayState.WORKED)) {
            return isWorkedFullDay() ? getSummaryWorkedTime() - day.getType().getWorkTimeInMSecond() : day.getType().getWorkTimeInMSecond() - getSummaryWorkedTime();
        }
        return 0L;
    }

}