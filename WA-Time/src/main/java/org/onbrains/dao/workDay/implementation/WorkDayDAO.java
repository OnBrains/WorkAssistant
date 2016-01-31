package org.onbrains.dao.workDay.implementation;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.onbrains.dao.workDay.WorkDayDAOInterface;
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

	@Override
	public WorkDay getWorkDay(Date day, Worker currentWorker) {
		WorkDay currentDay;
		try {
			currentDay = em.createNamedQuery(WorkDay.GET_WORK_DAY, WorkDay.class)
					.setParameter("worker", currentWorker).setParameter("day", day).getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
		return currentDay;
	}

	@Override
	public List<WorkDay> getWorkDaysByMonth(Date month, Worker currentWorker) {
		return em.createNamedQuery(WorkDay.GET_WORK_DAYS_BY_MONTH, WorkDay.class).setParameter("worker", currentWorker)
				.setParameter("month", month).getResultList();
	}

}