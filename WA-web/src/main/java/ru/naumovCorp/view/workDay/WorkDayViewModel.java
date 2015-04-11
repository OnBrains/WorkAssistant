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

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import java.util.ArrayList;
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

    private List<WorkDay> daysByMonthType;
    private Long idealWorkedTimeByAllMonth;
    private Long idealWorkedTimeByCurrentDay;
    private Long realWorkedTime;

    /**
     * Получает следующий месяц
     */
    public void nextMonth() {
        selectedMonth.add(Calendar.MONTH, 1);
        initializationDaysForMonth();
    }

    /**
     * Получает предыдущий месяц
     */
    public void previousMonth() {
        selectedMonth.add(Calendar.MONTH, -1);
        initializationDaysForMonth();
    }

    /**
     * Получаем все дни, из БД, по выбранному месяцу(selectedMonth)
     */
    private void initializationDaysForMonth() {
        cleanSummeryTimes();
        if (selectedMonth == null) {
            selectedMonth = Calendar.getInstance();
        }
        daysBySelectedMonth = wdDAO.getDayInfoByMonth(selectedMonth.getTime(), sUtil.getWorker());
        calculateDeltaTime();
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
            day.setType(DayType.WORK_DAY);
            if (isHoliday(calendar)) {
                day.setType(DayType.HOLIDAY);
            }
            day.setComingTime(calendar);
            day.setOutTime(calendar);
            wdDAO.create(day);
        }
        initializationDaysForMonth();
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

    private void cleanSummeryTimes() {
        idealWorkedTimeByAllMonth = 0L;
        idealWorkedTimeByCurrentDay = 0L;
        realWorkedTime = 0L;
    }

    private List<WorkDay> getDaysPriorCurrentDay() {
        List<WorkDay> daysPriorCurrentDay = new ArrayList<>();
        for (WorkDay wd: daysBySelectedMonth) {
            if (wd.getDay().getTime() < getCurrentDay().getDay().getTime()) {
                daysPriorCurrentDay.add(wd);
            }
            if (wd.getDay().equals(getCurrentDay().getDay()) && getCurrentDay().getState().equals(WorkDayState.WORKED)) {
                daysPriorCurrentDay.add(wd);
            }
        }
        return daysPriorCurrentDay;
    }

    private void getDaysByMonthType() {
        if (isCurrentMonth()) {
            daysByMonthType = getDaysPriorCurrentDay();
        }
        if (isLastMonth()) {
            daysByMonthType = getDaysBySelectedMonth();
        }
    }

    private void calculateDeltaTime() {
        getDaysByMonthType();
        if (daysByMonthType != null) {
            for (WorkDay wd : daysByMonthType) {
                realWorkedTime = realWorkedTime + wd.getSummaryWorkedTime();
                idealWorkedTimeByCurrentDay = idealWorkedTimeByCurrentDay + wd.getType().getWorkTimeInMSecond();
            }
            if (daysByMonthType.equals(daysBySelectedMonth)) {
                idealWorkedTimeByAllMonth = idealWorkedTimeByCurrentDay;
            } else {
                for (WorkDay wd: daysBySelectedMonth) {
                    idealWorkedTimeByAllMonth = idealWorkedTimeByAllMonth + wd.getType().getWorkTimeInMSecond();
                }
            }
        }
    }

    private boolean isCurrentMonth() {
        Calendar currentMonth = Calendar.getInstance();
        return currentMonth.YEAR == selectedMonth.YEAR && currentMonth.MONTH == selectedMonth.MONTH;
    }

    private boolean isLastMonth() {
        Calendar currentMonth = Calendar.getInstance();
        return selectedMonth.YEAR <= currentMonth.YEAR && selectedMonth.MONTH < currentMonth.MONTH;
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
            initializationDaysForMonth();
        }
        return daysBySelectedMonth;
    }

    private WorkDay getCurrentDay() {
        if (currentDay == null) {
            currentDay = wdDAO.getCurrentDayInfo(new Date(), sUtil.getWorker());
        }
        return currentDay;
    }

    public Long getIdealWorkedTimeByAllMonth() {
        return idealWorkedTimeByAllMonth;
    }

    public Long getIdealWorkedTimeByCurrentDay() {
        return idealWorkedTimeByCurrentDay;
    }

    public Long getRealWorkedTime() {
        return realWorkedTime;
    }
}