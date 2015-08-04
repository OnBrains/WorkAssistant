package org.onbrains.dao.workDayEvent;

import org.onbrains.entity.event.Event;

/**
 * @author Naumov Oleg on 04.08.2015 22:14.
 */
public interface EventDAOInterface {

    public void create(Event event);
    public void update(Event event);
    public void remove(Event event);
    public Event find(Long eventId);

}
