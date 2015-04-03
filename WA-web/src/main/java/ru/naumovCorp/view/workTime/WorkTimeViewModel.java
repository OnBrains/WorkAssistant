package ru.naumovCorp.view.workTime;


import ru.naumovCorp.dao.DAOHelper;
import ru.naumovCorp.entity.workTime.WorkDayState;
import ru.naumovCorp.entity.worker.Worker;
import ru.naumovCorp.dao.workTime.WorkTimeDAOInterface;
import ru.naumovCorp.entity.workTime.WorkTime;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.Calendar;
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

    private Calendar selectedMonth = Calendar.getInstance();
    private List<WorkTime> daysBySelectedMonth;
    private WorkTime currentDay;

    public Calendar getSelectedMonth() {
        return selectedMonth;
    }

    public void nextMonth() {
        selectedMonth.add(Calendar.MONTH, 1);
    }

    public void previousMonth() {
        selectedMonth.set(Calendar.MONTH, selectedMonth.get(Calendar.MONTH) - 1);
    }

    // TODO: сделать метод получающий данные не по тякущему мес, а по выбранному и завязать на выбиралку все.
    public List<WorkTime> getDaysBySelectedMonth() {
        if (daysBySelectedMonth == null && !selectedMonth.equals(Calendar.getInstance())) {
            daysBySelectedMonth = wtDAO.getTimeInfoByMonth(selectedMonth.getTime(), getCurrentWorker());
        }
        return daysBySelectedMonth;
    }

    public WorkTime getCurrentDay() {
        if (currentDay == null) {
            currentDay = wtDAO.getCurrentDayInfo(new Date(), getCurrentWorker());
        }
        return currentDay;
    }

    public WorkDayState[] getStates() {
        return WorkDayState.values();
    }

    // TODO: подумать как лучше передовать сюда значение мес, для которого надо создать дни
    // при переключении мес. запоминать его и предлогать создать дни???
    // TODO: вынести создание дней в DAO и сделать метод транзакционным?
    public void createWorkDaysMonth() {
        Calendar calendar = Calendar.getInstance();
        for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i);
            WorkTime day = new WorkTime(getCurrentWorker(), calendar.getTime(), false);
            if (isHoliday(calendar)) {
                day.setHoliday(true);
            }
            wtDAO.create(day);
        }
    }

    /**
     * Проверяет является ли создаваемый день субботой или воскресеньем
     * @return true если выходной
     */
    private boolean isHoliday(Calendar calendar) {
        if (calendar.get(Calendar.DAY_OF_WEEK) != 6
                && calendar.get(Calendar.DAY_OF_WEEK) != 7) {
            return false;
        } else {
            return true;
        }
    }

    //TODO: при реализации авторизации, переделать на залогиненого работника
    private Worker getCurrentWorker() {
        return dh.getEntityManager().find(Worker.class, 1L);
    }

}