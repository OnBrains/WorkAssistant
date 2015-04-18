package ru.naumovCorp.entity.workDay;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import ru.naumovCorp.entity.worker.Worker;

import static ru.naumovCorp.entity.workDay.WorkDayState.NO_WORK;

/**
 * @author Naumov Oleg on 21.03.2015 21:20.
 */

@Entity
@Table(name = "WORK_DAY", uniqueConstraints = {@UniqueConstraint(columnNames = {"WORKER_ID", "DAY"})})
@NamedQueries
    ({
        @NamedQuery(name = WorkDay.GET_TIME_INFO_BY_MONTH,
                query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day, 'yyyyMM') = to_char(:month, 'yyyyMM') order by wt.day"),
        @NamedQuery(name = WorkDay.GET_CURRENT_DAY,
                query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day, 'yyyyMMdd') = to_char(:day, 'yyyyMMdd')"),
        @NamedQuery(name = WorkDay.GET_DAYS_PRIOR_CURRENT_DAY,
                query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day, 'yyyyMM') = to_char(:month, 'yyyyMM') and wt.day <= :month order by wt.day")
    })

public class WorkDay implements Serializable {

    public static final String GET_TIME_INFO_BY_MONTH = "WorkTimeDAO.getDayInfoByMonth";
    public static final String GET_CURRENT_DAY = "WorkTimeDAO.getCurrentDay";
    public static final String GET_DAYS_PRIOR_CURRENT_DAY = "WorkTimeDAO.getDaysPriorCurrentDay";
    public static final Long mSecondsInWorkDay = 30600000L;

    @Id
    @GeneratedValue(generator = "WorkTimeId")
    @SequenceGenerator(name = "WorkTimeId", sequenceName = "GEN_WORK_DAY_ID", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKER_ID", nullable = false)
    private Worker worker;

    @Column(name = "DAY", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date day;

    @Enumerated(EnumType.STRING)
    @Column(name = "DAY_TYPE", length = 16, nullable = false)
    private DayType type;

    @Column(name = "COMING_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar comingTime;

    @Column(name = "OUT_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar outTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", length = 16, nullable = false)
    private WorkDayState state = NO_WORK;

    @Transient
    private Long summaryWorkedTime;

    @Transient
    private boolean workedFullDay;

    @Transient
    private Long deltaTime;

    protected WorkDay() {
    }

    public WorkDay(Worker worker, Date day) {
        this.worker = worker;
        this.day = day;
        //TODO: надо ли сетить состояние тут?
        this.state = WorkDayState.NO_WORK;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public DayType getType() {
        return type;
    }

    public void setType(DayType type) {
        this.type = type;
    }

    public Calendar getComingTime() {
        return comingTime;
    }

    public void setComingTime(Calendar comingTime) {
        this.comingTime = comingTime;
    }

    public Calendar getOutTime() {
        return outTime;
    }

    public void setOutTime(Calendar outTime) {
        this.outTime = outTime;
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
        return getSummaryWorkedTime() > type.getWorkTimeInMSecond();
    }

    /**
     * Вычисляет переработанное или недоработанное время, для завершенного дня.
     *
     * @return - переработанное/недоработанное время в мил. секундах, если день не закончен вернет 0
     */
    public Long getDeltaTime() {
        if (getState().equals(WorkDayState.WORKED)) {
            return isWorkedFullDay() ? getSummaryWorkedTime() - type.getWorkTimeInMSecond() : type.getWorkTimeInMSecond() - getSummaryWorkedTime();
        }
        return 0L;
    }

    public WorkDayState getState() {
        return state;
    }

    public void setState(WorkDayState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkDay workDay = (WorkDay) o;

        if (id != null ? !id.equals(workDay.id) : workDay.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}