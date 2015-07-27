package org.onbrains.dao.workDay;

import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.worker.Worker;

import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 22.03.2015 14:41.
 */

public interface WorkDayDAOInterface {

    public void create(WorkDay workDay);
    public void update(WorkDay workDay);
    public void remove(WorkDay workDay);
    public WorkDay find(Long workDayId);
    public WorkDay getCurrentDayInfo(Date day, Worker currentWorker);
    public List<WorkDay> getDayInfoByMonth(Date month, Worker currentWorker);
    public List<WorkDay> getDaysPriorCurrentDay(Date month, Worker currentWorker);

}