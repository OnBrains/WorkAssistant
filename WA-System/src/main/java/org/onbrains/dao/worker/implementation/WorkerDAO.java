package org.onbrains.dao.worker.implementation;

import org.onbrains.dao.DAOHelper;
import org.onbrains.dao.worker.WorkerDAOInterface;
import org.onbrains.entity.worker.Worker;

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