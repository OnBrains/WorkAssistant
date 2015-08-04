package org.onbrains.viewModel.workDay;

import org.onbrains.dao.day.DayDAOInterface;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.day.Day;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.utils.parsing.DateFormatService;
import org.onbrains.service.SessionUtil;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Naumov Oleg on 30.07.2015 21:43.
 */
@ManagedBean
@ViewScoped
public class WorkDayViewModel {

    @Inject
    private WorkDayDAOInterface wdDAO;
    @Inject
    private DayDAOInterface dDAO;
    @Inject
    private SessionUtil sessionUtil;

    private Calendar selectedMonth = Calendar.getInstance();
    private List<WorkDay> daysBySelectedMonth = new ArrayList<>();

    @PostConstruct
    private void postConstruct() {
        initializationWorkDayForSelectedMonth();
    }

    /**
     * Увеличивает значение выбранного месяца на один месяц.
     */
    public void nextMonth() {
        selectedMonth.add(Calendar.MONTH, 1);
        initializationWorkDayForSelectedMonth();
    }

    /**
     * Уменьшает значение выбранного месяца на один месяц.
     */
    public void previousMonth() {
        selectedMonth.add(Calendar.MONTH, -1);
        initializationWorkDayForSelectedMonth();
    }

    /**
     * Инициализирует коллекцию рабочих дней для выбранного {@linkplain #getSelectedMonth() месяца}
     * и выбранного {@linkplain #getSelectedMonth() месяца}
     */
    private void initializationWorkDayForSelectedMonth() {
        daysBySelectedMonth = wdDAO.getDayInfoByMonth(getSelectedMonth().getTime(), sessionUtil.getWorker());
    }

    /**
     * Создает {@linkplain WorkDay рабочие дни} для выбранного месяца
     * и {@linkplain SessionUtil#getWorker()  авторизованного пользователя}.
     * Пологаем, что создаем все рабочии дни, не должно быть такой ситуации,
     * когда заведена только часть рабочих дней.
     */
    public void createWorkDaysForSelectedMonth() {
        List<Day> daysBySelectedMonth = new ArrayList<>();
        daysBySelectedMonth = dDAO.getDaysByMonth(getSelectedMonth().getTime());
        for (Day day : daysBySelectedMonth) {
            wdDAO.create(new WorkDay(sessionUtil.getWorker(), day));
        }
    }

    /**
     * Simple getters and setters
     */

    /**
     * Выбранный месяц, для которого надо отобразить рабочии дни, в случае если месяц не выбран,
     * то берется текущий.
     *
     * @return Значение текущего месяца в формате Calendar.
     */
    public Calendar getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(Calendar selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    /**
     * Список всех рабочих дней для выбранного месяца.
     *
     * @return список рабочих дней.
     */
    public List<WorkDay> getWorkDaysBySelectedMonth() {
        return daysBySelectedMonth;
    }

    public void setWorkDaysBySelectedMonth(List<WorkDay> daysBySelectedMonth) {
        this.daysBySelectedMonth = daysBySelectedMonth;
    }

    /**
     * Получает информацию о следующим за {@linkplain #getSelectedMonth() выбранным месяцем}.
     *
     * @return Информация о следующем месяце.
     */
    public String getNameForNextMonth() {
        Calendar nextMonth = Calendar.getInstance();
        nextMonth.setTime(getSelectedMonth().getTime());
        nextMonth.add(Calendar.MONTH, 1);
        return DateFormatService.toMMMMMYYYY(nextMonth.getTime());
    }

    /**
     * Получает информацию о предыдущем относительно {@linkplain #getSelectedMonth() выбранного месяца}.
     *
     * @return Информация о предыдущем месяце.
     */
    public String getNameForPreviousMonth() {
        Calendar previousMonth = Calendar.getInstance();
        previousMonth.setTime(getSelectedMonth().getTime());
        previousMonth.add(Calendar.MONTH, -1);
        return DateFormatService.toMMMMMYYYY(previousMonth.getTime());
    }
}
