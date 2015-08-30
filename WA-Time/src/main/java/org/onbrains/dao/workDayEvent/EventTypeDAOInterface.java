package org.onbrains.dao.workDayEvent;

import org.onbrains.entity.event.EventType;

import java.util.List;

/**
 * @author Naumov Oleg on 04.08.2015 22:18.
 */
public interface EventTypeDAOInterface {

	public List<EventType> getAllEventTypes();

}
