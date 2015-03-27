package ru.naumovCorp.dao.workTime.implementation;

import ru.naumovCorp.dao.DAOHelper;
import ru.naumovCorp.dao.workTime.WorkTimeDAOInterface;
import ru.naumovCorp.entity.workTime.WorkTime;
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
public class WorkTimeDAO implements WorkTimeDAOInterface {

    @Inject
    private DAOHelper dh;

    @Override
    public void create(WorkTime workTime) {
        dh.persist(workTime);
    }

    @Override
    public void update(WorkTime workTime) {
        dh.merge(workTime);
    }

    @Override
    public void remove(WorkTime workTime) {
        dh.remove(workTime);
    }

    @Override
    public WorkTime getCurrentDayInfo(Date day, Worker currentWorker) {
        EntityManager em = dh.getEntityManager();
        WorkTime currentDay;
        try {
            currentDay = em.createNamedQuery(WorkTime.GET_CURRENT_DAY, WorkTime.class)
                    .setParameter("worker", currentWorker).setParameter("day", day).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
        return currentDay;
    }

    @Override
    public List<WorkTime> getTimeInfoByMonth(Date month, Worker currentWorker) {
        EntityManager em = dh.getEntityManager();
        return em.createNamedQuery(WorkTime.GET_TIME_INFO_BY_MONTH, WorkTime.class)
                .setParameter("worker", currentWorker).setParameter("month", month).getResultList();
    }

}