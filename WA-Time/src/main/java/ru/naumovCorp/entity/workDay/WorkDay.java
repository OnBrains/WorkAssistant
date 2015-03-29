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
@Table(name = "WORK_DAY", uniqueConstraints = {@UniqueConstraint(columnNames = {"DAY"})})
@NamedQueries
    ({
        @NamedQuery(name = WorkDay.GET_TIME_INFO_BY_MONTH,
                query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day, 'yyyyMM') = to_char(:month, 'yyyyMM') order by wt.day"),
        @NamedQuery(name = WorkDay.GET_CURRENT_DAY,
                query = "select wt from WorkDay wt where wt.worker = :worker and to_char(wt.day, 'yyyyMMdd') = to_char(:day, 'yyyyMMdd')")
    })

// TODO: переименовать в WorkDay
public class WorkDay implements Serializable {

    public static final String GET_TIME_INFO_BY_MONTH = "WorkTimeDAO.getDayInfoByMonth";
    public static final String GET_CURRENT_DAY = "WorkTimeDAO.getCurrentDay";
    public static final Long mSecondsInWorkDay = 30600000L;

    @Id
    @GeneratedValue(generator = "WorkTimeId")
    @SequenceGenerator(name = "WorkTimeId", sequenceName = "GEN_WORK_DAY_ID", allocationSize = 1)
    @Column(name = "WORK_DAY_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKER_ID", nullable = false)
    private Worker worker;

    @Column(name = "DAY", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date day;

    @Column(name = "IS_HOLIDAY", nullable = false)
    private boolean isHoliday;

    @Column(name = "COMING_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar comingTime;

    @Column(name = "OUT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar outTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", length = 16)
    private WorkDayState state = NO_WORK;

    @Transient
    private Long summaryWorkedTime;

    protected WorkDay() {
    }

    public WorkDay(Worker worker, Date day, boolean isHoliday) {
        this.worker = worker;
        this.day = day;
        this.isHoliday = isHoliday;
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

    public boolean isHoliday() {
        return isHoliday;
    }

    public void setHoliday(boolean isHoliday) {
        this.isHoliday = isHoliday;
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

    public Long getSummaryWorkedTime() {
        return outTime.getTimeInMillis() - comingTime.getTimeInMillis();
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