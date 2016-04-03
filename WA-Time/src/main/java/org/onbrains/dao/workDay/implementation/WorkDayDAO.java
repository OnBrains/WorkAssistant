package org.onbrains.dao.workDay.implementation;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.onbrains.dao.day.DayDAOInterface;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.day.Day;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.worker.Worker;

/**
 * @author Naumov Oleg on 22.03.2015 14:47.
 */

@Stateless
public class WorkDayDAO implements WorkDayDAOInterface, Serializable {

	private static final long serialVersionUID = -1L;

	@PersistenceContext(unitName = "WA")
	private EntityManager em;

    @Inject
    private DayDAOInterface dDAO;

	@Override
	public WorkDay getWorkDay(LocalDate day, Worker currentWorker) {
		WorkDay currentDay;
		try {
            Day d = (Day) em.createQuery("from Day where day = :ddd").setParameter("ddd", day).getSingleResult();
			currentDay = em.createNamedQuery(WorkDay.GET_WORK_DAY, WorkDay.class)
					.setParameter("worker", currentWorker).setParameter("day", d).getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
		return currentDay;
	}

	@Override
	public List<WorkDay> getWorkDaysByMonth(LocalDate month, Worker currentWorker) {
		return em.createNamedQuery(WorkDay.GET_WORK_DAYS_BY_MONTH, WorkDay.class).setParameter("worker", currentWorker)
				.setParameter("month", month.format(DateTimeFormatter.ofPattern("yyyyMM"))).getResultList();
	}

}