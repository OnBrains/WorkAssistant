package org.onbrains.viewModel.workDay;

import static org.onbrains.utils.parsing.DateFormatService.toMMMMMYYYY;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
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
import org.onbrains.utils.information.Notification;
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

	private LocalDate selectedMonth = LocalDate.now();
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
		selectedMonth.plusMonths(1);
		initWorkDays();
		initStatisticService();
	}

	/**
	 * Получает название следующего за {@linkplain #getSelectedMonth() выбранным месяцем}.
	 *
	 * @return Название следующего месяца.
	 */
	public String getNameForNextMonth() {
		LocalDate nextMonth = getSelectedMonth().plusMonths(1);
		return toMMMMMYYYY(nextMonth);
	}

	/**
	 * Уменьшает значение выбранного месяца на один месяц.
	 */
	public void previousMonth() {
		selectedMonth.minusMonths(1);
		initWorkDays();
		initStatisticService();
	}

	/**
	 * Получает название предыдущего относительно {@linkplain #getSelectedMonth() выбранного месяца}.
	 *
	 * @return Название предыдущего месяце.
	 */
	public String getNameForPreviousMonth() {
		LocalDate previousMonth = getSelectedMonth().minusMonths(1);
		return toMMMMMYYYY(previousMonth);
	}

	/**
	 * Создает {@linkplain WorkDay рабочие дни} для выбранного месяца и {@linkplain SessionUtil#getWorker()
	 * авторизованного пользователя}. Пологаем, что создаем все рабочии дни, не должно быть такой ситуации, когда
	 * заведена только часть рабочих дней.
	 */
	public void createWorkDays() {
		List<Day> daysBySelectedMonth = dDAO.getDaysByMonth(getSelectedMonth());
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
			currentWorkDay = wdDAO.getWorkDay(LocalDate.now(), SessionUtil.getWorker());
		}
		return currentWorkDay;
	}

	/**
	 * Инициализирует коллекцию рабочих дней для выбранного {@linkplain #getSelectedMonth() месяца} и выбранного
	 * {@linkplain #getSelectedMonth() месяца}
	 */
	private void initWorkDays() {
		if (selectedMonth == null) {
			selectedMonth = LocalDate.now();
		}
		worDays = wdDAO.getWorkDaysByMonth(getSelectedMonth(), SessionUtil.getWorker());
	}

	private void initStatisticService() {
		if (!worDays.isEmpty()) {
			statisticService = isCurrentMonth() ? new CurrentMonthStatisticService(getWorkDays())
					: new MonthStatisticService(getWorkDays());
		}
	}

	private boolean isCurrentMonth() {
		// Calendar currentMonth = Calendar.getInstance();
		LocalDate currentMonth = LocalDate.now();
		return currentMonth.get(ChronoField.YEAR) == selectedMonth.get(ChronoField.YEAR)
				&& currentMonth.get(ChronoField.MONTH_OF_YEAR) == selectedMonth.get(ChronoField.MONTH_OF_YEAR);
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

	/**
	 * Выбранный месяц, для которого надо отобразить рабочии дни, в случае если месяц не выбран, то берется текущий.
	 *
	 * @return Значение текущего месяца в формате Calendar.
	 */
	public LocalDate getSelectedMonth() {
		return selectedMonth;
	}

	public void setSelectedMonth(LocalDate selectedMonth) {
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