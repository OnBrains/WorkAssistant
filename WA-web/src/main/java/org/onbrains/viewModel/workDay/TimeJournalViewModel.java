package org.onbrains.viewModel.workDay;

import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.dao.day.DayDAOInterface;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.day.Day;
import org.onbrains.entity.workDay.DayType;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.parsing.DateFormatService;
import org.primefaces.event.RowEditEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 30.07.2015 21:43.
 */
@Named
@ConversationScoped
public class TimeJournalViewModel implements Serializable {

    @Inject
	private EntityManagerUtils em;
	@Inject
	private WorkDayDAOInterface wdDAO;
	@Inject
	private DayDAOInterface dDAO;
	@Inject
	private SessionUtil sessionUtil;
    @Inject
    private MonthStatistic statistic;

	private Calendar selectedMonth = Calendar.getInstance();
	private List<WorkDay> daysBySelectedMonth = new ArrayList<>();
	private WorkDay currentWorkDay;

	@PostConstruct
	private void postConstruct() {
		initializationWorkDayForSelectedMonth();
        statistic.calculateStatistic(getWorkDaysBySelectedMonth(), getDaysByMonthType());
	}

	/**
	 * Увеличивает значение выбранного месяца на один месяц.
	 */
	public void nextMonth() {
		selectedMonth.add(Calendar.MONTH, 1);
		initializationWorkDayForSelectedMonth();
        statistic.calculateStatistic(getWorkDaysBySelectedMonth(), getDaysByMonthType());
	}

	/**
	 * Уменьшает значение выбранного месяца на один месяц.
	 */
	public void previousMonth() {
		selectedMonth.add(Calendar.MONTH, -1);
		initializationWorkDayForSelectedMonth();
        statistic.calculateStatistic(getWorkDaysBySelectedMonth(), getDaysByMonthType());
	}

    /**
     * Изменяем запись в таблице
     *
     * @param event
     *            - запись, которая редактируется
     */
    public void onRowEdit(RowEditEvent event) {
        WorkDay day = (WorkDay) event.getObject();
        em.merge(day);
        statistic.calculateStatistic(getWorkDaysBySelectedMonth(), getDaysByMonthType());
    }

	/**
	 * Инициализирует коллекцию рабочих дней для выбранного {@linkplain #getSelectedMonth() месяца} и выбранного
	 * {@linkplain #getSelectedMonth() месяца}
	 */
	private void initializationWorkDayForSelectedMonth() {
        if (selectedMonth == null) {
            selectedMonth = Calendar.getInstance();
        }
		daysBySelectedMonth = wdDAO.getDayInfoByMonth(getSelectedMonth().getTime(), SessionUtil.getWorker());
	}

    private List<WorkDay> getDaysPriorCurrentDay() {
        List<WorkDay> daysPriorCurrentDay = new ArrayList<>();
        for (WorkDay wd : daysBySelectedMonth) {
            if (wd.getDay().getDay().getTime() < getCurrentDay().getDay().getDay().getTime()) {
                daysPriorCurrentDay.add(wd);
            }
            if (wd.getDay().equals(getCurrentDay().getDay())
                    && getCurrentDay().getState().equals(WorkDayState.WORKED)) {
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
            return getWorkDaysBySelectedMonth();
        }
        return null;
    }

	/**
	 * Создает {@linkplain WorkDay рабочие дни} для выбранного месяца и {@linkplain SessionUtil#getWorker()
	 * авторизованного пользователя}. Пологаем, что создаем все рабочии дни, не должно быть такой ситуации, когда
	 * заведена только часть рабочих дней.
	 */
	public void createWorkDaysForSelectedMonth() {
		List<Day> daysBySelectedMonth = new ArrayList<>();
		daysBySelectedMonth = dDAO.getDaysByMonth(getSelectedMonth().getTime());
		for (Day day : daysBySelectedMonth) {
			em.persist(new WorkDay(sessionUtil.getWorker(), day));
		}
        initializationWorkDayForSelectedMonth();
        statistic.calculateStatistic(getWorkDaysBySelectedMonth(), getDaysByMonthType());
	}

	/**
	 * Simple getters and setters
	 */

	/**
	 * Выбранный месяц, для которого надо отобразить рабочии дни, в случае если месяц не выбран, то берется текущий.
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

	/**
	 * Style
	 */

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
    // Методы для получения стилей блока статистики
    // =================================================================================================================

    private float getPercentage(Long fullTime, Long partTime) {
        return (float) partTime * 100 / fullTime;
    }

    public String getStyleForWorkedTime() {
        String display = statistic.getWorkedTime() == 0 ? "none" : "table-cell";
        return "background-color: #4da9f1;padding: 4px; width: "
                + getPercentage(statistic.getSummaryTime(), statistic.getWorkedTime()) + "%; display: " + display + ";";
    }

    public String getStyleForDeltaTime() {
        String color = statistic.isRealWorkedTimeMoreIdeal() ? "green" : "red";
        String display = statistic.getDeltaTime() == 0 ? "none" : "table-cell";
        return "background-color: " + color + "; padding: 4px;width: "
                + getPercentage(statistic.getSummaryTime(), statistic.getDeltaTime()) + "%; display: " + display + ";";
    }

    public String getStyleForRemainingTime() {
        String display = statistic.getRemainingTime() == 0 ? "none" : "table-cell";
        return "background-color: chocolate; padding: 4px;width: "
                + getPercentage(statistic.getSummaryTime(), statistic.getRemainingTime()) + "%; display: " + display
                + ";";
    }

	/**
	 * Simple getters & setters
	 */

	private WorkDay getCurrentDay() {
		if (currentWorkDay == null) {
			currentWorkDay = wdDAO.getCurrentDayInfo(new Date(), sessionUtil.getWorker());
		}
		return currentWorkDay;
	}

    public MonthStatistic getStatistic() {
        return statistic;
    }

}
