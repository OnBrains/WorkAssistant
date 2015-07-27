package org.onbrains.dao.worker;

import org.onbrains.entity.worker.Worker;

import java.util.List;

/**
 * @author Naumov Oleg on 21.03.2015 20:53.
 */
public interface WorkerDAOInterface {

    public void create(Worker worker);
    public void update(Worker worker);
    public void remove(Worker worker);
    public List<Worker> getWorkers();

}
