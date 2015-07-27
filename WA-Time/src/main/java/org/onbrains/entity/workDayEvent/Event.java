package org.onbrains.entity.workDayEvent;

import org.onbrains.entity.worker.Worker;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 18.04.2015 14:59.
 */

@Entity
@Table(name = "EVENT")
public class Event implements Serializable {

    @Id
    @GeneratedValue(generator = "EventId")
    @SequenceGenerator(name = "EventId", sequenceName = "GEN_EVENT_ID", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DAY", nullable = false)
    private Date day;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "WORKER_EVENT", joinColumns = {@JoinColumn(name = "EVENT_ID")},
                                        inverseJoinColumns = {@JoinColumn(name = "WORKER_ID")})
    private List<Worker> workDays = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_ID", nullable = false)
    private EventType type;

    @Column(name = "TITLE", nullable = false, length = 128)
    private String title;

    @Column(name = "DESCRIPTION", nullable = true, length = 512)
    private String description;

    @Column(name = "FULL_DAY", nullable = false)
    private Boolean isFullDay = false;

    @Column(name = "START_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startTime;

    @Column(name = "END_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endTime;

    protected Event() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public List<Worker> getWorkDays() {
        return workDays;
    }

    public void setWorkDays(List<Worker> workDays) {
        this.workDays = workDays;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsFullDay() {
        return isFullDay;
    }

    public void setIsFullDay(Boolean isFullDay) {
        this.isFullDay = isFullDay;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

}