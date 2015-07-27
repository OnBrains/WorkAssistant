package org.onbrains.dao.workDay.implementation;

import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.worker.Worker;
import org.onbrains.dao.DAOHelper;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 22.03.2015 14:47.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class WorkDayDAO implements WorkDayDAOInterface {

    @Inject
    private DAOHelper dh;

    @Override
    public void create(WorkDay workDay) {
        dh.persist(workDay);
    }

    @Override
    public void update(WorkDay workDay) {
        dh.merge(workDay);
    }

    @Override
    public void remove(WorkDay workDay) {
        dh.remove(workDay);
    }

    @Override
    public WorkDay find(Long workDayId) {
        EntityManager em = dh.getEntityManager();
        return em.find(WorkDay.class, workDayId);
    }

    @Override
    public WorkDay getCurrentDayInfo(Date day, Worker currentWorker) {
        EntityManager em = dh.getEntityManager();
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
        EntityManager em = dh.getEntityManager();
        return em.createNamedQuery(WorkDay.GET_TIME_INFO_BY_MONTH, WorkDay.class)
                .setParameter("worker", currentWorker).setParameter("month", month).getResultList();
    }

    @Override
    public List<WorkDay> getDaysPriorCurrentDay(Date month, Worker currentWorker) {
        EntityManager em = dh.getEntityManager();
        return em.createNamedQuery(WorkDay.GET_DAYS_PRIOR_CURRENT_DAY, WorkDay.class)
                .setParameter("month", month).setParameter("worker", currentWorker).getResultList();
    }
}