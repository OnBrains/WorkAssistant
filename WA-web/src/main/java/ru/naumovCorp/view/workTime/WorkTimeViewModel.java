package ru.naumovCorp.view.workTime;


import ru.naumovCorp.dao.DAOHelper;
import ru.naumovCorp.entity.workTime.WorkDayState;
import ru.naumovCorp.entity.worker.Worker;
import ru.naumovCorp.dao.workTime.WorkTimeDAOInterface;
import ru.naumovCorp.entity.workTime.WorkTime;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 22.03.2015 14:31.
 */

@ManagedBean
@ViewScoped
public class WorkTimeViewModel {

    @Inject
    private WorkTimeDAOInterface wtDAO;
    @Inject
    private DAOHelper dh;

    private List<WorkTime> timeByCurrentMonth;

    public List<WorkTime> getTimeByCurrentMonth() {
        if (timeByCurrentMonth == null) {
            timeByCurrentMonth = wtDAO.getTimeInfoByMonth(new Date(), getCurrentWorker());
        }
        return timeByCurrentMonth;
    }

    public WorkDayState[] getStates() {
        return WorkDayState.values();
    }

    //TODO: при реализации авторизации, переделать на залогиненого работника
    private Worker getCurrentWorker() {
        return dh.getEntityManager().find(Worker.class, 1L);
    }

}