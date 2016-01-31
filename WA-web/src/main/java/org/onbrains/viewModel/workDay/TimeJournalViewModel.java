package org.onbrains.viewModel.workDay;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.onbrains.component.statistic.StatisticValue;
import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.dao.day.DayDAOInterface;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.day.Day;
import org.onbrains.entity.workDay.DayType;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.parsing.DateFormatService;
import org.onbrains.viewModel.workDay.monthStatistic.CurrentMonthStatisticService;
import org.onbrains.viewModel.workDay.monthStatistic.MonthStatisticService;

/**
 * Модель для списка рабочих дней месяца.
 * <p/>
 * 
 * @author Naumov Oleg on 30.07.2015 21:43.
 */
@Named
@ViewScoped
public class TimeJournalViewModel implements Serializable {

	@Inject
	private EntityManagerUtils em;
	@Inject
	private WorkDayDAOInterface wdDAO;
	@Inject
	private DayDAOInterface dDAO;

	private MonthStatisticService statisticService;

	private Calendar selectedMonth = Calendar.getInstance();
	private List<WorkDay> worDays;
	private WorkDay currentWorkDay;

	@PostConstruct
	private void postConstruct() {
		initWorkDays();
		initStatisticService();
	}

	/**
	 * Увеличивает значение выбранного месяца на один месяц.
	 */
	public void nextMonth() {
		selectedMonth.add(Calendar.MONTH, 1);
		initWorkDays();
		initStatisticService();
	}

	/**
	 * Получает название следующего за {@linkplain #getSelectedMonth() выбранным месяцем}.
	 *
	 * @return Название следующего месяца.
	 */
	public String getNameForNextMonth() {
		Calendar nextMonth = Calendar.getInstance();
		nextMonth.setTime(getSelectedMonth().getTime());
		nextMonth.add(Calendar.MONTH, 1);
		return DateFormatService.toMMMMMYYYY(nextMonth.getTime());
	}

	/**
	 * Уменьшает значение выбранного месяца на один месяц.
	 */
	public void previousMonth() {
		selectedMonth.add(Calendar.MONTH, -1);
		initWorkDays();
		initStatisticService();
	}

	/**
	 * Получает название предыдущего относительно {@linkplain #getSelectedMonth() выбранного месяца}.
	 *
	 * @return Название предыдущего месяце.
	 */
	public String getNameForPreviousMonth() {
		Calendar previousMonth = Calendar.getInstance();
		previousMonth.setTime(getSelectedMonth().getTime());
		previousMonth.add(Calendar.MONTH, -1);
		return DateFormatService.toMMMMMYYYY(previousMonth.getTime());
	}

	/**
	 * Создает {@linkplain WorkDay рабочие дни} для выбранного месяца и {@linkplain SessionUtil#getWorker()
	 * авторизованного пользователя}. Пологаем, что создаем все рабочии дни, не должно быть такой ситуации, когда
	 * заведена только часть рабочих дней.
	 */
	public void createWorkDays() {
		List<Day> daysBySelectedMonth = dDAO.getDaysByMonth(getSelectedMonth().getTime());
		for (Day day : daysBySelectedMonth) {
			em.persist(new WorkDay(SessionUtil.getWorker(), day));
		}
		initWorkDays();
		initStatisticService();
	}

	/**
	 * Статистика отработанного времени за выбранный месяц. Состоит из следующих элементов:
	 * <ul>
	 * <li>Отработано - отработанное время;</li>
	 * <li>Переработок/Недоработок - дельта, говорящая о переработках или недоработках времени;</li>
	 * <li>Осталось - оставшееся время, которое необходимо отработать.</li>
	 * </ul>
	 * 
	 * @return Список элементов статистики.
	 */
	public List<StatisticValue> getWorkDayStatistic() {
		List<StatisticValue> workDayStatistic = new LinkedList<>();
		if (!worDays.isEmpty()) {
			workDayStatistic.add(new StatisticValue(statisticService.getWorkedTime(), "Отработано", "#4da9f1"));
			workDayStatistic.add(statisticService.isPositiveDelta()
					? new StatisticValue(statisticService.getDeltaTime(), "Переработок", "green")
					: new StatisticValue(statisticService.getDeltaTime(), "Недоработок", "red"));
			workDayStatistic.add(new StatisticValue(statisticService.getRemainderTime(), "Осталось", "chocolate"));
		}
		return workDayStatistic;
	}

	/**
	 * return Класс для стиля строки в таблице рабочих дней месяца.
	 */
	public String getStyleClassForRow(WorkDay workDay) {
		if (workDay.getDay().getType().equals(DayType.HOLIDAY) && !workDay.equals(getCurrentDay())) {
			return "color_for_holiday";
		}
		if (workDay.equals(getCurrentDay())) {
			return "color_for_current_day";
		}
		return "";
	}

    public String getDeltaTimeStyle(WorkDay workDay) {
        return workDay.isWorkedFullDay() ? "color: green;" : "color: red;";
    }

	// *****************************************************************************************************************
	// Private methods
	// *****************************************************************************************************************

	private WorkDay getCurrentDay() {
		if (currentWorkDay == null) {
			currentWorkDay = wdDAO.getWorkDay(new Date(), SessionUtil.getWorker());
		}
		return currentWorkDay;
	}

	/**
	 * Инициализирует коллекцию рабочих дней для выбранного {@linkplain #getSelectedMonth() месяца} и выбранного
	 * {@linkplain #getSelectedMonth() месяца}
	 */
	private void initWorkDays() {
		if (selectedMonth == null) {
			selectedMonth = Calendar.getInstance();
		}
		worDays = wdDAO.getWorkDaysByMonth(getSelectedMonth().getTime(), SessionUtil.getWorker());
	}

	private void initStatisticService() {
		if (!worDays.isEmpty()) {
			statisticService = isCurrentMonth() ? new CurrentMonthStatisticService(getWorkDays())
					: new MonthStatisticService(getWorkDays());
		}
	}

	private boolean isCurrentMonth() {
		Calendar currentMonth = Calendar.getInstance();
		return currentMonth.get(Calendar.YEAR) == selectedMonth.get(Calendar.YEAR)
				&& currentMonth.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH);
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

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
	public List<WorkDay> getWorkDays() {
		return worDays;
	}

}