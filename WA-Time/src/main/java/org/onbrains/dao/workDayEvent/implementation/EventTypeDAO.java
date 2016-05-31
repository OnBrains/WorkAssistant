package org.onbrains.dao.workDayEvent.implementation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.EventType;

/**
 * @author Naumov Oleg on 04.08.2015 22:19.
 */
@Stateless
public class EventTypeDAO implements EventTypeDAOInterface, Serializable {

	private static final long serialVersionUID = -3929071148318505888L;

	@PersistenceContext(unitName = "WA")
	private EntityManager em;

	@Override
	public List<EventType> getEventTypes(Boolean... activeFlags) {
		return em.createNamedQuery(EventType.GET_EVENT_TYPES, EventType.class)
				.setParameter("activeFlags", Arrays.asList(activeFlags)).getResultList();
	}

}