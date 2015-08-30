package org.onbrains.dao.worker.implementation;

import org.onbrains.dao.worker.WorkerDAOInterface;
import org.onbrains.entity.worker.Worker;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Naumov Oleg on 21.03.2015 20:57.
 */

@Stateless
public class WorkerDAO implements WorkerDAOInterface {

    @PersistenceContext(unitName = "WA")
    private EntityManager em;

    @Override
    public List<Worker> getWorkers() {
        return em.createNamedQuery(Worker.GET_ALL_WORKER, Worker.class).getResultList();
    }

}