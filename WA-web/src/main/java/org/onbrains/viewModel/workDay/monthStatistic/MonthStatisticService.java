package org.onbrains.viewModel.workDay.monthStatistic;

import java.util.Calendar;
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
			idealWorkedTimeForMonth = idealWorkedTimeForMonth + workDay.getDay().getType().getWorkTimeInMSecond();
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
		Calendar currentDate = Calendar.getInstance();
		Calendar selectedMonth = Calendar.getInstance();
		selectedMonth.setTime(workDays.get(0).getDay().getDay());
		return currentDate.get(Calendar.YEAR) < selectedMonth.get(Calendar.YEAR)
				|| (currentDate.get(Calendar.YEAR) == selectedMonth.get(Calendar.YEAR)
						&& currentDate.get(Calendar.MONTH) < selectedMonth.get(Calendar.MONTH));
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