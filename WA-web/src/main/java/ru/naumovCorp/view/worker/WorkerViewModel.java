package ru.naumovCorp.view.worker;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

import org.primefaces.event.RowEditEvent;
import ru.naumovCorp.dao.worker.WorkerDAOInterface;
import ru.naumovCorp.entity.worker.Worker;

/**
 * @author Naumov Oleg on 21.03.2015 20:34.
 */

@ManagedBean
@ViewScoped
public class WorkerViewModel implements Serializable {

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
        wDAO.update(worker);
    }

}