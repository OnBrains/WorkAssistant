package org.onbrains.viewModel.worker;

import org.onbrains.dao.worker.WorkerDAOInterface;
import org.onbrains.entity.worker.Worker;
import org.primefaces.event.RowEditEvent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

/**
 * @author Naumov Oleg on 21.03.2015 20:34.
 */

@ManagedBean
@ViewScoped
public class WorkerViewModel implements Serializable {

    @PersistenceContext
    private EntityManager em;
    @Inject
    private WorkerDAOInterface wDAO;

    private List<Worker> workers;

    public List<Worker> getWorkers() {
        if (workers == null) {
            workers = wDAO.getWorkers();
        }
        return workers;
    }

    public void onRowEdit(RowEditEvent event) {
        Worker worker = (Worker) event.getObject();
        em.merge(worker);
    }

}