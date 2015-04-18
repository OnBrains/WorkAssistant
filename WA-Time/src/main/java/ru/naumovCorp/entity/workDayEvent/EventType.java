package ru.naumovCorp.entity.workDayEvent;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 18.04.2015 13:41.
 */

@Entity
@Table(name = "EVENT_TYPE")
public class EventType implements Serializable {

    @Id
    @GeneratedValue(generator = "EventTypeId")
    @SequenceGenerator(name = "EventTypeId", sequenceName = "GEN_EVENT_TYPE_ID", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 16)
    private String name;

    @Column(name = "IS_WORKING", nullable = false)
    private boolean isWorking;

    @Column(name = "TIME", nullable = true)
    private Long time = 0L;

    protected EventType() {
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