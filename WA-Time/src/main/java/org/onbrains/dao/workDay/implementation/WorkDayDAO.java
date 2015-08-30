package org.onbrains.dao.workDay.implementation;

import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.worker.Worker;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 22.03.2015 14:47.
 */

@Stateless
public class WorkDayDAO implements WorkDayDAOInterface, Serializable {

	private static final long serialVersionUID = -1L;

	@PersistenceContext(unitName = "WA")
	private EntityManager em;

	@Override
	public WorkDay getCurrentDayInfo(Date day, Worker currentWorker) {
		WorkDay currentDay;
		try {
			currentDay = em.createNamedQuery(WorkDay.GET_CURRENT_DAY, WorkDay.class)
					.setParameter("worker", currentWorker).setParameter("day", day).getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
		return currentDay;
	}

	@Override
	public List<WorkDay> getDayInfoByMonth(Date month, Worker currentWorker) {
		return em.createNamedQuery(WorkDay.GET_WORK_DAYS_BY_MONTH, WorkDay.class).setParameter("worker", currentWorker)
				.setParameter("month", month).getResultList();
	}

	@Override
	public List<WorkDay> getDaysPriorCurrentDay(Date month, Worker currentWorker) {
		return em.createNamedQuery(WorkDay.GET_DAYS_PRIOR_CURRENT_DAY, WorkDay.class).setParameter("month", month)
				.setParameter("worker", currentWorker).getResultList();
	}

}