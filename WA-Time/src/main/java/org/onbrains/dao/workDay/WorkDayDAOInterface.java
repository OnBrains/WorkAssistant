package org.onbrains.dao.workDay;

import java.util.Date;
import java.util.List;

import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.worker.Worker;

/**
 * @author Naumov Oleg on 22.03.2015 14:41.
 */

public interface WorkDayDAOInterface {

	public WorkDay getWorkDay(Date day, Worker currentWorker);

	public List<WorkDay> getWorkDaysByMonth(Date month, Worker currentWorker);

}