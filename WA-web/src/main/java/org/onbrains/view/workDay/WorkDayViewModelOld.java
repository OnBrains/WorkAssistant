package org.onbrains.view.workDay;

import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.workDay.DayType;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.utils.information.Notification;
import org.onbrains.service.SessionUtil;
import org.primefaces.event.RowEditEvent;
import org.onbrains.utils.parsing.DateFormatService;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 22.03.2015 14:31.
 */

@ManagedBean
@ViewScoped
public class WorkDayViewModelOld {

    @Inject
    private WorkDayDAOInterface wdDAO;
    @Inject
    private SessionUtil sUtil;

    private Calendar selectedMonth;
    private List<WorkDay> daysBySelectedMonth;
    private WorkDay currentDay;
    private List<WorkDay> selectedDays;

    @Inject
    private MonthStatistic statistic;

    @PostConstruct
    private void postConstruct() {
        statistic.calculateStatistic(getDaysBySelectedMonth(), getDaysByMonthType());
    }

    /**
     * Получает следующий месяц
     */
    public void nextMonth() {
        selectedMonth.add(Calendar.MONTH, 1);
        initializationDaysForMonth();
        statistic.calculateStatistic(getDaysBySelectedMonth(), getDaysByMonthType());
    }

    /**
     * Получает предыдущий месяц
     */
    public void previousMonth() {
        selectedMonth.add(Calendar.MONTH, -1);
        initializationDaysForMonth();
        statistic.calculateStatistic(getDaysBySelectedMonth(), getDaysByMonthType());
    }

    /**
     * Получаем все дни, из БД, по выбранному месяцу(selectedMonth)
     */
    private void initializationDaysForMonth() {
        if (selectedMonth == null) {
            selectedMonth = Calendar.getInstance();
        }
        daysBySelectedMonth = wdDAO.getDayInfoByMonth(selectedMonth.getTime(), sUtil.getWorker());
    }

    /**
     * @return массив возможных состояний рабочего дня
     */
    public WorkDayState[] getStates() {
        return WorkDayState.values();
    }

    public DayType[] getDayTypes() {
        return DayType.values();
    }

    public void changeDaysType(DayType newType) {
        for (WorkDay wd : selectedDays) {
            wd.getDay().setType(newType);
            wdDAO.update(wd);
        }
        statistic.calculateStatistic(getDaysBySelectedMonth(), getDaysByMonthType());
    }

    // TODO: вынести создание дней в DAO и сделать метод транзакционным?

    /**
     * Создает все дни, для выбранного месяца (selectedMonth)
     */
    public void createDaysForMonth() {
        Calendar calendar = setZeroTime(selectedMonth);
        for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i);
//            WorkDay day = new WorkDay(sUtil.getWorker(), calendar.getTime());
//            day.getDay().setType(DayType.WORK_DAY);
//            if (isHoliday(calendar)) {
//                day.getDay().setType(DayType.HOLIDAY);
//            }
//            day.setComingTime(calendar);
//            day.setOutTime(calendar);
//            wdDAO.create(day);
        }
        initializationDaysForMonth();
        statistic.calculateStatistic(getDaysBySelectedMonth(), getDaysByMonthType());
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
        return calendar.get(Calendar.DAY_OF_WEEK) == 1 || calendar.get(Calendar.DAY_OF_WEEK) == 7;
    }

    private boolean isComingTimeMoreOutTime(WorkDay workDay) {
        return workDay.getComingTime().getTimeInMillis() > workDay.getOutTime().getTimeInMillis();
    }

    /**
     * Изменяем запись в таблице
     *
     * @param event - запись, которая редактируется
     */
    //TODO: прикрутить логику редактирования
    public void onRowEdit(RowEditEvent event) {
        WorkDay day = (WorkDay) event.getObject();
        WorkDay oldDayState = wdDAO.find(day.getId());
        if (isComingTimeMoreOutTime(day) && day.getState().equals(WorkDayState.WORKED)) {
            WorkDay editDay = null;
            for (WorkDay wd: daysBySelectedMonth) {
                if (wd.equals(oldDayState)) {
                    editDay = wd;
                }
            }
            Notification.info("Нельзя сохранить изменения", "Время начала рабочего дня больше времени окончания");
            editDay.setComingTime(oldDayState.getComingTime());
            editDay.setOutTime(oldDayState.getOutTime());
            editDay.setState(oldDayState.getState());
            return;
        }
        day.setComingTime(updateYear(day, day.getComingTime()));
        day.setOutTime(updateYear(day, day.getOutTime()));
        wdDAO.update(day);
        statistic.calculateStatistic(getDaysBySelectedMonth(), getDaysByMonthType());
    }

    /**
     * В Primefaces если изменять только время, без года, то проставляется 1970 год.
     * Если ортредактированно время прихода/ухода, то надо проставить год обратно.
     */
    private Calendar updateYear(WorkDay workDay, Calendar calendar) {
        Calendar currentDay = Calendar.getInstance();
        currentDay.setTime(workDay.getDay().getDay());
        calendar.set(currentDay.get(currentDay.YEAR), currentDay.get(currentDay.MONTH), currentDay.get(currentDay.DATE));
        return calendar;
    }

    private List<WorkDay> getDaysPriorCurrentDay() {
        List<WorkDay> daysPriorCurrentDay = new ArrayList<>();
        for (WorkDay wd : daysBySelectedMonth) {
            if (wd.getDay().getDay().getTime() < getCurrentDay().getDay().getDay().getTime()) {
                daysPriorCurrentDay.add(wd);
            }
            if (wd.getDay().equals(getCurrentDay().getDay()) && getCurrentDay().getState().equals(WorkDayState.WORKED)) {
                daysPriorCurrentDay.add(wd);
            }
        }
        return daysPriorCurrentDay;
    }

    private boolean isCurrentMonth() {
        Calendar currentMonth = Calendar.getInstance();
        return currentMonth.YEAR == selectedMonth.YEAR && currentMonth.MONTH == selectedMonth.MONTH;
    }

    private boolean isLastMonth() {
        Calendar currentMonth = Calendar.getInstance();
        return selectedMonth.YEAR <= currentMonth.YEAR && selectedMonth.MONTH < currentMonth.MONTH;
    }

    private List<WorkDay> getDaysByMonthType() {
        if (isCurrentMonth()) {
            return getDaysPriorCurrentDay();
        }
        if (isLastMonth()) {
            return getDaysBySelectedMonth();
        }
        return null;
    }

    // =================================================================================================================
    // Методы для получения стилей блока статистики
    // =================================================================================================================

    private float getPercentage(Long fullTime, Long partTime) {
        return (float) partTime * 100 / fullTime;
    }

    public String getStyleForWorkedTime() {
        String display = statistic.getWorkedTime() == 0 ? "none" : "table-cell";
        return "background-color: #4da9f1;padding: 4px; width: " + getPercentage(statistic.getSummaryTime(), statistic.getWorkedTime()) + "%; display: " + display + ";";
    }

    public String getStyleForDeltaTime() {
        String color = statistic.isRealWorkedTimeMoreIdeal() ? "green" : "red";
        String display = statistic.getDeltaTime() == 0 ? "none" : "table-cell";
        return "background-color: " + color + "; padding: 4px;width: " + getPercentage(statistic.getSummaryTime(), statistic.getDeltaTime()) + "%; display: " + display + ";";
    }

    public String getStyleForRemainingTime() {
        String display = statistic.getRemainingTime() == 0 ? "none" : "table-cell";
        return "background-color: chocolate; padding: 4px;width: " + getPercentage(statistic.getSummaryTime(), statistic.getRemainingTime()) + "%; display: " + display + ";";
    }

    // =================================================================================================================
    // Методы для работы отображения времени в UI + стили
    // =================================================================================================================

    public int getMinHourForOutTime(WorkDay workDay) {
        Calendar calendar = Calendar.getInstance();
        return workDay.getComingTime().get(Calendar.HOUR);
    }

    /**
     * @param date - дата в полном формате
     * @return - строковое значение времени
     */
    public String getTime(Date date) {
        return DateFormatService.toHHMM(date);
    }

    public String getTime(Long mSecond) {
        return DateFormatService.mSecToHHMM(mSecond);
    }

    public String dateFormatForDayInTable(Date date) {
        return DateFormatService.toDDEE(date);
    }

    public String dateFormatForFeaderTable(Date date) {
        return DateFormatService.toDDEE(date);
    }

    public String getStyleClassForRow(WorkDay dayInfo) {
        if (dayInfo.getDay().getType().equals(DayType.HOLIDAY) && !dayInfo.equals(getCurrentDay())) {
            return "color_for_holiday";
        }
        if (dayInfo.equals(getCurrentDay())) {
            return "color_for_current_day";
        } else {
            return "";
        }
    }

    // =================================================================================================================
    // Simple getters and setters
    // =================================================================================================================

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

    public List<WorkDay> getSelectedDays() {
        return selectedDays;
    }

    public void setSelectedDays(List<WorkDay> selectedDays) {
        this.selectedDays = selectedDays;
    }

    public MonthStatistic getStatistic() {
        return statistic;
    }

    public String getNameNextMonth() {
        Calendar nextMonth = Calendar.getInstance();
        nextMonth.setTime(selectedMonth.getTime());
        nextMonth.add(Calendar.MONTH, 1);
        return dateFormatForFeaderTable(nextMonth.getTime());
    }

    public String getNamePreviousMonth() {
        Calendar previousMonth = Calendar.getInstance();
        previousMonth.setTime(selectedMonth.getTime());
        previousMonth.add(Calendar.MONTH, -1);
        return dateFormatForFeaderTable(previousMonth.getTime());
    }

}