package org.onbrains.dao.workDayEvent;

import org.onbrains.entity.event.EventType;

import java.util.List;

/**
 * @author Naumov Oleg on 04.08.2015 22:18.
 */
public interface EventTypeDAOInterface {

    public void create(EventType eventType);
    public void update(EventType eventType);
    public void remove(EventType eventType);
    public EventType find(Long eventTypeId);
    public List<EventType> getAllEventType();

}
