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
import java.util.LinkedList;
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

    private Calendar selectedMonth;
    private List<WorkTime> daysBySelectedMonth;
    private WorkTime currentDay;

    /**
     * Получает следующий месяц
     */
    public void nextMonth() {
        selectedMonth.add(Calendar.MONTH, 1);
        daysBySelectedMonth = initializationDaysForMonth();
    }

    /**
     * Получает предыдущий месяц
     */
    public void previousMonth() {
        selectedMonth.add(Calendar.MONTH, -1);
        daysBySelectedMonth = initializationDaysForMonth();
    }

    /**
     * Получаем все дни, из БД, по выбранному месяцу(selectedMonth)
     */
    private List<WorkTime> initializationDaysForMonth() {
        if (selectedMonth == null) {
            selectedMonth = Calendar.getInstance();
        }
        return wtDAO.getTimeInfoByMonth(selectedMonth.getTime(), getCurrentWorker());
    }

    public WorkTime getCurrentDay() {
        if (currentDay == null) {
            currentDay = wtDAO.getCurrentDayInfo(new Date(), getCurrentWorker());
        }
        return currentDay;
    }

    /**
     * @return массив возможных состояний рабочего дня
     */
    public WorkDayState[] getStates() {
        return WorkDayState.values();
    }

    // TODO: вынести создание дней в DAO и сделать метод транзакционным?
    /**
     * Создает все дни, для выбранного месяца (selectedMonth)
     */
    public void createDaysForMonth() {
        Calendar calendar = selectedMonth;
        for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i);
            WorkTime day = new WorkTime(getCurrentWorker(), calendar.getTime(), false);
            if (isHoliday(calendar)) {
                day.setHoliday(true);
            }
            wtDAO.create(day);
        }
        daysBySelectedMonth = initializationDaysForMonth();
    }

    /**
     * Проверяет является ли создаваемый день субботой или воскресеньем
     *
     * @return true если выходной
     */
    private boolean isHoliday(Calendar calendar) {
        if (calendar.get(Calendar.DAY_OF_WEEK) != 1
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

    /*******************************************************************************************************************
     * Simple getters and setters
     ******************************************************************************************************************/

    public Calendar getSelectedMonth() {
        return selectedMonth;
    }

    public List<WorkTime> getDaysBySelectedMonth() {
        if (daysBySelectedMonth == null) {
            daysBySelectedMonth = initializationDaysForMonth();
        }
        return daysBySelectedMonth;
    }

}