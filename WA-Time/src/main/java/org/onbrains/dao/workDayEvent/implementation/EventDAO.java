package org.onbrains.dao.workDayEvent.implementation;

import org.onbrains.dao.DAOHelper;
import org.onbrains.dao.workDayEvent.EventDAOInterface;
import org.onbrains.entity.event.Event;

import javax.inject.Inject;

/**
 * @author Naumov Oleg on 04.08.2015 22:27.
 */
public class EventDAO implements EventDAOInterface {

    @Inject
    private DAOHelper dh;

    @Override
    public void create(Event event) {
        dh.persist(event);
    }

    @Override
    public void update(Event event) {
        dh.merge(event);
    }

    @Override
    public void remove(Event event) {
        dh.remove(event);
    }

    @Override
    public Event find(Long eventId) {
        return dh.getEntityManager().find(Event.class, eventId);
    }

}
