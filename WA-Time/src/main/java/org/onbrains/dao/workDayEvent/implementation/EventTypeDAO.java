package org.onbrains.dao.workDayEvent.implementation;

import org.onbrains.dao.DAOHelper;
import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.EventType;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import java.util.List;

/**
 * @author Naumov Oleg on 04.08.2015 22:19.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class EventTypeDAO implements EventTypeDAOInterface {

	@Inject
	private DAOHelper dh;

	@Override
	public void create(EventType eventType) {
		dh.persist(eventType);
	}

	@Override
	public void update(EventType eventType) {
		dh.merge(eventType);
	}

	@Override
	public void remove(EventType eventType) {
		dh.remove(eventType);
	}

	@Override
	public EventType find(Long eventTypeId) {
		return dh.getEntityManager().find(EventType.class, eventTypeId);
	}

	@Override
	public List<EventType> getAllEventType() {
		return dh.getEntityManager().createNamedQuery(EventType.GET_ALL_EVENT_TYPE, EventType.class).getResultList();
	}
}
