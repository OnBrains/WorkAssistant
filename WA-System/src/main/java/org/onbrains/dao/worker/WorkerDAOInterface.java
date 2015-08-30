package org.onbrains.dao.worker;

import org.onbrains.entity.worker.Worker;

import java.util.List;

/**
 * @author Naumov Oleg on 21.03.2015 20:53.
 */
public interface WorkerDAOInterface {

    public List<Worker> getWorkers();

}
