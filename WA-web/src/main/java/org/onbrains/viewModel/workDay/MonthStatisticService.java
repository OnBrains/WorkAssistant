package org.onbrains.viewModel.workDay;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;

import org.onbrains.entity.workDay.WorkDay;

/**
 * Сервис для расчёта статистики отработанного времени за месяц. Данный сервис считать основным, он должен
 * использоваться для расчета статистики всех месяцев за исключением текущего рабочего месяца.
 * <p/>
 * Created by Oleg Naumov on 24.01.2016.
 */
public class MonthStatisticService {

	protected long workedTime;
	protected long deltaTime;
	protected long remainderTime;
	protected boolean isPositiveDelta;

	protected long idealWorkedTimeForMonth;
	protected long realWorkedTime;
	protected List<WorkDay> workDays;

	public MonthStatisticService(List<WorkDay> workDays) {
		this.workDays = workDays;
		calculate();
	}

	protected void calculate() {
		initRawData();
		initWorkedTime();
		initDeltaTime();
		initRemainderTime();
		initDeltaType();
	}

	protected void initRawData() {
		for (WorkDay workDay : workDays) {
			idealWorkedTimeForMonth = idealWorkedTimeForMonth + workDay.getDay().getType().getWorkTimeInSecond();
			if (workDay.isWorked()) {
				realWorkedTime = realWorkedTime + workDay.getWorkedTime();
			}
		}
	}

	protected void initWorkedTime() {
		workedTime = isWorkedFullMonth() ? idealWorkedTimeForMonth : realWorkedTime;
	}

	protected void initDeltaTime() {
		deltaTime = !isFutureMonth() ? Math.abs(realWorkedTime - idealWorkedTimeForMonth) : 0;
	}

	protected void initRemainderTime() {
		remainderTime = !isFutureMonth() ? 0 : idealWorkedTimeForMonth - realWorkedTime;
	}

	protected void initDeltaType() {
		isPositiveDelta = isWorkedFullMonth();
	}

	protected boolean isWorkedFullMonth() {
		return realWorkedTime > idealWorkedTimeForMonth;
	}

	private boolean isFutureMonth() {
		LocalDate currentDate = LocalDate.now();
		LocalDate selectedMonth = workDays.get(0).getDay().getDate();
		return currentDate.get(ChronoField.YEAR) < selectedMonth.get(ChronoField.YEAR)
				|| (currentDate.get(ChronoField.YEAR) == selectedMonth.get(ChronoField.YEAR)
						&& currentDate.get(ChronoField.MONTH_OF_YEAR) < selectedMonth.get(ChronoField.MONTH_OF_YEAR));
	}

	public long getWorkedTime() {
		return workedTime;
	}

	public long getDeltaTime() {
		return deltaTime;
	}

	public long getRemainderTime() {
		return remainderTime;
	}

	public boolean isPositiveDelta() {
		return isPositiveDelta;
	}

}