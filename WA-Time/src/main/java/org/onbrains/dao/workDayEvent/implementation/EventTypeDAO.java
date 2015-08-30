package org.onbrains.dao.workDayEvent.implementation;

import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.EventType;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * @author Naumov Oleg on 04.08.2015 22:19.
 */
@Stateless
public class EventTypeDAO implements EventTypeDAOInterface, Serializable {

    private static final long serialVersionUID = -1L;

	@PersistenceContext(unitName = "WA")
	private EntityManager em;

	@Override
	public List<EventType> getAllEventType() {
		return em.createNamedQuery(EventType.GET_ALL_EVENT_TYPE, EventType.class).getResultList();
	}

}
