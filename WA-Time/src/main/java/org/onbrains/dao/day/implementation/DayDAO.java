package org.onbrains.dao.day.implementation;

import org.onbrains.dao.day.DayDAOInterface;
import org.onbrains.entity.day.Day;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Naumov Oleg on 31.07.2015 0:04.
 */
@Stateless
public class DayDAO implements DayDAOInterface, Serializable {

    private static final long serialVersionUID = -1L;

	@PersistenceContext(unitName = "WA")
	private EntityManager em;

	@Override
	public List<Day> getDaysByMonth(LocalDate month) {
		return em.createNamedQuery(Day.GET_DAYS_BY_MONTH, Day.class).setParameter("month", Date.valueOf(month)).getResultList();
	}
}
