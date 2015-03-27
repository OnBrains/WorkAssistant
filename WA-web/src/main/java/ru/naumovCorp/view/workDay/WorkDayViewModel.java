package ru.naumovCorp.view.workDay;


import org.primefaces.event.RowEditEvent;
import ru.naumovCorp.dao.DAOHelper;
import ru.naumovCorp.entity.workDay.WorkDayState;
import ru.naumovCorp.entity.worker.Worker;
import ru.naumovCorp.dao.workDay.WorkDayDAOInterface;
import ru.naumovCorp.entity.workDay.WorkDay;
import ru.naumovCorp.parsing.ConvertDate;

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
public class WorkDayViewModel {

    @Inject
    private WorkDayDAOInterface wdDAO;
    @Inject
    private DAOHelper dh;

    private Calendar selectedMonth;
    private List<WorkDay> daysBySelectedMonth;
    private WorkDay currentDay;

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
    private List<WorkDay> initializationDaysForMonth() {
        if (selectedMonth == null) {
            selectedMonth = Calendar.getInstance();
        }
        return wdDAO.getDayInfoByMonth(selectedMonth.getTime(), getCurrentWorker());
    }

    public WorkDay getCurrentDay() {
        if (currentDay == null) {
            currentDay = wdDAO.getCurrentDayInfo(new Date(), getCurrentWorker());
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
        Calendar calendar = setZeroTime(selectedMonth);
        for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i);
            WorkDay day = new WorkDay(getCurrentWorker(), calendar.getTime(), false);
            if (isHoliday(calendar)) {
                day.setHoliday(true);
            }
            day.setComingTime(calendar);
            day.setOutTime(calendar);
            wdDAO.create(day);
        }
        daysBySelectedMonth = initializationDaysForMonth();
    }

    private Calendar setZeroTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
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

    /**
     * Изменяем запись в таблице
     *
     * @param event - запись, которая редактируется
     */
    //TODO: прикрутить логику редактирования
    public void onRowEdit(RowEditEvent event) {
        wdDAO.update((WorkDay) event.getObject());
    }

    /**
     * @param date - дата в полном формате
     * @return - строковое значение времени
     */
    public String getTime(Date date) {
        return ConvertDate.getTime(date);
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

    public List<WorkDay> getDaysBySelectedMonth() {
        if (daysBySelectedMonth == null) {
            daysBySelectedMonth = initializationDaysForMonth();
        }
        return daysBySelectedMonth;
    }

}