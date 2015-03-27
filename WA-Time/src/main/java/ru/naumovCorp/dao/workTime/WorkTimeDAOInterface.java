package ru.naumovCorp.dao.workTime;

import ru.naumovCorp.entity.workTime.WorkTime;
import ru.naumovCorp.entity.worker.Worker;

import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 22.03.2015 14:41.
 */

public interface WorkTimeDAOInterface {

    public void create(WorkTime workTime);
    public void update(WorkTime workTime);
    public void remove(WorkTime workTime);
    public WorkTime getCurrentDayInfo(Date day, Worker currentWorker);
    public List<WorkTime> getTimeInfoByMonth(Date month, Worker currentWorker);

}
