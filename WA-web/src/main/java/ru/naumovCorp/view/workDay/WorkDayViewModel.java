package ru.naumovCorp.view.workDay;


import org.primefaces.event.RowEditEvent;
import ru.naumovCorp.dao.DAOHelper;
import ru.naumovCorp.entity.workDay.DayType;
import ru.naumovCorp.entity.workDay.WorkDayState;
import ru.naumovCorp.entity.worker.Worker;
import ru.naumovCorp.dao.workDay.WorkDayDAOInterface;
import ru.naumovCorp.entity.workDay.WorkDay;
import ru.naumovCorp.parsing.ConvertDate;
import ru.naumovCorp.service.SessionUtil;

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
    private SessionUtil sUtil;

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
        return wdDAO.getDayInfoByMonth(selectedMonth.getTime(), sUtil.getWorker());
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
            WorkDay day = new WorkDay(sUtil.getWorker(), calendar.getTime(), false);
            if (isHoliday(calendar)) {
                day.setType(DayType.HOLIDAY);
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
     * Вычисляет результирующие переработки/недоработки за месяц.
     * Вычисляется до текущего дня. Складываются все отработанное время за эти дни и сравнивается за идеальным.
     *
     * @return - итоговоые переработки/недоработки за месяц
     */
    //TODO: нужно по разному вычислять для текущего мес. и для всех остальных
    public String getMonthDeltaTime() {
        return "00:00";
    }

//    public boolean workedAllTime() {
//        return realWorkedTime >= idealWorkedTime;
//    }

    private Long calculateDeltaTime(List<WorkDay> days) {
        Long idealWorkedTime = 0L;
        Long realWorkedTime = 0L;
        for (WorkDay wd: days) {
            if (!wd.isHoliday()) {
                realWorkedTime = realWorkedTime + wd.getSummaryWorkedTime();
                idealWorkedTime = idealWorkedTime + WorkDay.mSecondsInWorkDay;
            }
        }
        return realWorkedTime > idealWorkedTime ? realWorkedTime - idealWorkedTime : idealWorkedTime - realWorkedTime;
    }

    /**
     * @param date - дата в полном формате
     * @return - строковое значение времени
     */
    public String getTime(Date date) {
        return ConvertDate.getTime(date);
    }

    public String getTime(Long mSecond) {
        return ConvertDate.formattedTimeFromMSec(mSecond);
    }

    public String dateFormatForDayInTable(Date date) {
        return ConvertDate.dateFormatWithWeekDay(date);
    }

    public String dateFormatForFeaderTable(Date date) {
        return ConvertDate.dateFormatMonthYear(date);
    }

    public String getStyleClassForRow(WorkDay dayInfo) {
        if (dayInfo.getType().equals(DayType.HOLIDAY) && !dayInfo.equals(getCurrentDay())) {
            return "color_for_holiday";
        } if (dayInfo.equals(getCurrentDay())) {
            return "color_for_current_day";
        } else {
            return "";
        }
    }

    public String getStyleForDeltaTime(WorkDay dayInfo) {
        return dayInfo.isWorkedFullDay() ? "color: green;" : "color: red;";
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

    private WorkDay getCurrentDay() {
        if (currentDay == null) {
            currentDay = wdDAO.getCurrentDayInfo(new Date(), sUtil.getWorker());
        }
        return currentDay;
    }

}