package org.onbrains.dao.workDay;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.worker.Worker;

/**
 * @author Naumov Oleg on 22.03.2015 14:41.
 */

public interface WorkDayDAOInterface {

	public WorkDay getWorkDay(LocalDate day, Worker currentWorker);

	public List<WorkDay> getWorkDaysByMonth(LocalDate month, Worker currentWorker);

}