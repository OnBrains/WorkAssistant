package org.onbrains.viewModel.workDay.monthStatistic;

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
	}

	public void calculate() {
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
				realWorkedTime = remainderTime + workDay.getSummaryWorkedTime();
			}
		}
	}

	protected void initWorkedTime() {
		workedTime = isWorkedFullMonth() ? idealWorkedTimeForMonth : realWorkedTime;
	}

	protected void initDeltaTime() {
		deltaTime = Math.abs(realWorkedTime - idealWorkedTimeForMonth);
	}

	protected void initRemainderTime() {
		remainderTime = isWorkedFullMonth() ? 0 : idealWorkedTimeForMonth - realWorkedTime;
	}

	protected void initDeltaType() {
		isPositiveDelta = isWorkedFullMonth();
	}

	protected boolean isWorkedFullMonth() {
		return realWorkedTime > idealWorkedTimeForMonth;
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