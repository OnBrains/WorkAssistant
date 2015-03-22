package ru.naumovCorp.dao.worker.implementation;

import dao.DAOHelper;
import ru.naumovCorp.dao.worker.WorkerDAOInterface;
import ru.naumovCorp.entity.worker.Worker;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import java.util.List;

/**
 * @author Naumov Oleg on 21.03.2015 20:57.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@NamedQuery(name = WorkerDAO.GET_ALL_WORKER, query = "select w from Worker w")
public class WorkerDAO implements WorkerDAOInterface {

    public static final String GET_ALL_WORKER = "WorkerDAO.getWorkers";

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
        return em.createNamedQuery(GET_ALL_WORKER, Worker.class).getResultList();
    }

}
