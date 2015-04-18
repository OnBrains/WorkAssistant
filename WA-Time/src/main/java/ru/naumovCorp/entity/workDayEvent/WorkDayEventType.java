package ru.naumovCorp.entity.workDayEvent;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 18.04.2015 13:41.
 */

@Entity
@Table(name = "WORK_DAY_EVENT_TYPE", uniqueConstraints = {@UniqueConstraint(columnNames = "")})
public class WorkDayEventType implements Serializable {

    @Id
    @GeneratedValue(generator = "WorkDayEventId")
    @SequenceGenerator(name = "WorkDayEventId", sequenceName = "GEN_WORK_DAY_EVENT_ID", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 16)
    private String name;

    @Column(name = "IS_WORKING", nullable = false)
    private boolean isWorking;

    @Column(name = "TIME", nullable = true)
    private Long time = 0L;

    protected WorkDayEventType() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

}