package ru.naumovCorp.dao.workDay.implementation;

import ru.naumovCorp.dao.DAOHelper;
import ru.naumovCorp.dao.workDay.WorkDayDAOInterface;
import ru.naumovCorp.entity.workDay.WorkDay;
import ru.naumovCorp.entity.worker.Worker;

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

}