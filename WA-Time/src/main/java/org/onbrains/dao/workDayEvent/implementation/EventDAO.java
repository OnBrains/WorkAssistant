package org.onbrains.dao.workDayEvent.implementation;

import org.onbrains.dao.workDayEvent.EventDAOInterface;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 04.08.2015 22:27.
 */

@Stateless
public class EventDAO implements EventDAOInterface, Serializable {

    private static final long serialVersionUID = -1L;

	@PersistenceContext(unitName = "WA")
	private EntityManager em;

}
