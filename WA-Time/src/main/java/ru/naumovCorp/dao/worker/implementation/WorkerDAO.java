package ru.naumovCorp.dao.worker.implementation;

import dao.DAOHelper;
import ru.naumovCorp.dao.worker.WorkerDAOInterface;
import ru.naumovCorp.entity.worker.Worker;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Naumov Oleg on 21.03.2015 20:57.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class WorkerDAO implements WorkerDAOInterface {

    @Inject
    DAOHelper dh;

    @Override
    public void create(Worker worker) {
        dh.persist(worker);
    }

    @Override
    public void update(Worker worker) {
        dh.merge(worker);
    }

    @Override
    public void remove(Worker worker) {
        dh.remove(worker);
    }

    @Override
    public List<Worker> getWorkers() {
        EntityManager em = dh.getEntityManager();
        return em.createNamedQuery(Worker.GET_ALL_WORKER, Worker.class).getResultList();
    }

}