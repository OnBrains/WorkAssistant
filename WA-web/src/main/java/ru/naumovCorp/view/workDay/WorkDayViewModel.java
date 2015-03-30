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
     * Обработка изменения состояния по текущему дню
     */
    public void currentDayStateChangeHandle() {
        if (currentDay.getState().equals(WorkDayState.WORKING)) {
            workDayStart();
        } else if (currentDay.getState().equals(WorkDayState.WORKED)) {
            workDayEnd();
        }
        wdDAO.update(currentDay);
    }

    /**
     * Проставляет время прихода для текущего дня, проставляется текущее время
     */
    private void workDayStart() {
        currentDay.setComingTime(Calendar.getInstance());
    }

    /**
     * Проставляет время ухода для текущего дня, проставляется текущее время
     */
    private void workDayEnd() {
        currentDay.setOutTime(Calendar.getInstance());
    }

    /**
     * Возвращает отформатированное время прихода, для текущего дня
     *
     * @return - если день не найден, то "-  "
     */
    public String getComingTimeByCurrentDay() {
        if (getCurrentDay() != null && isCurrentDayStart()) {
            return getTime(currentDay.getComingTime().getTime());
        } else {
            return "-";
        }
    }

    public boolean isCurrentDayEnd() {
        return currentDay.getState().equals(WorkDayState.WORKED);
    }

    private boolean isCurrentDayStart() {
        return !currentDay.getState().equals(WorkDayState.NO_WORK);
    }

    public String getOutTimeForCurrentDay() {
        if (isCurrentDayStart()) {
            if (isCurrentDayEnd()) {
                return getRealOutTime();
            } else {
                return getPossibleOutTime();
            }
        } else {
            return "-";
        }
    }

    private String getRealOutTime() {
        return getTime(currentDay.getOutTime().getTime());
    }

    /**
     * Вычисляет возможное время ухода, в зависимости от времени прихода
     *
     * @return - время прихода + 8.5 часов, если день не найден, то "-"
     */
    //TODO: переименовать, так как тут могуть быть как недоработки так и переработки
    private String getPossibleOutTime() {
        if (getCurrentDay() != null) {
            Calendar possibleOutComeTime = Calendar.getInstance();
            possibleOutComeTime.setTimeInMillis(getPossibleOutTimeInMSecond());
            return getTime(possibleOutComeTime.getTime());
        } else {
            return "-";
        }
    }

    /**
     * @return - возможное время ухода в милисекундах
     */
    private Long getPossibleOutTimeInMSecond() {
        if (getCurrentDay() != null) {
            Long commingTimeInMSeconds = getCurrentDay().getComingTime().getTimeInMillis();
            Long possibleOutTimeInMSecond = commingTimeInMSeconds + WorkDay.mSecondsInWorkDay;
            return possibleOutTimeInMSecond;
        } else {
            return 0L;
        }
    }

    public String getResultWorkedTimeForCurrentDay() {
        if (isCurrentDayStart()) {
            if (isCurrentDayEnd()) {
                return getResultWorkedTime();
            } else {
                return getTimeForEndWorkDay();
            }
        } else {
            return "-";
        }
    }

    private String getResultWorkedTime() {
        return ConvertDate.formattedTimeFromMSec(currentDay.getSummaryWorkedTime());
    }

    /**
     * Вычисляет оставщееся время до окончания рабочего дня или переработанное время
     *
     * @return - оставшееся/переработанное время, если день не найден, то "-"
     */
    private String getTimeForEndWorkDay() {
        if (getCurrentDay() != null) {
            Calendar diffTime = Calendar.getInstance();
            Long possibleOutTime = getPossibleOutTimeInMSecond();
            Long diffTimeInMSecond;
            if (!ifCurrentTimeMoreOutTime()) {
                diffTimeInMSecond = possibleOutTime - diffTime.getTimeInMillis();
            } else {
                diffTimeInMSecond = diffTime.getTimeInMillis() - possibleOutTime;
            }
            return ConvertDate.formattedTimeFromMSec(diffTimeInMSecond);
        } else {
            return "-";
        }
    }

    /**
     * Сравнивает текущее время с возможным временем ухода для текущего дня
     *
     * @return - true если текущее время больше
     */
    public boolean ifCurrentTimeMoreOutTime() {
        Calendar currentTime = Calendar.getInstance();
        if (getCurrentDay() != null && currentTime.getTimeInMillis() > getPossibleOutTimeInMSecond()) {
            return true;
        } else {
            return false;
        }
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
        WorkDay day = (WorkDay) event.getObject();
        day.getDay();
        wdDAO.update((WorkDay) event.getObject());
    }

    /**
     * @param date - дата в полном формате
     * @return - строковое значение времени
     */
    public String getTime(Date date) {
        return ConvertDate.getTime(date);
    }

    public String dateFormatForDayInTable(Date date) {
        return ConvertDate.dateFormatWithWeekDay(date);
    }

    public String dateFormatForFeaderTable(Date date) {
        return ConvertDate.dateFormatMonthYear(date);
    }

    //TODO: при реализации авторизации, переделать на залогиненого работника
    private Worker getCurrentWorker() {
        return dh.getEntityManager().find(Worker.class, 1L);
    }

    /**
     * ****************************************************************************************************************
     * Simple getters and setters
     * ****************************************************************************************************************
     */

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