package org.onbrains.viewModel.workDay;

import java.time.LocalDate;
import java.util.List;

import org.onbrains.entity.workDay.WorkDay;

/**
 * Сервис для расчёта статистики отработанного времени для текущего месяца.
 * <p/>
 * Created by Oleg Naumov on 25.01.2016.
 */
public class CurrentMonthStatisticService extends MonthStatisticService {

	private long idealWorkedTimeUpToCurrentDay;

	public CurrentMonthStatisticService(List<WorkDay> workDays) {
		super(workDays);
	}

	@Override
	protected void initRawData() {
		LocalDate currentDay = LocalDate.now();
		for (WorkDay workDay : workDays) {
			idealWorkedTimeForMonth = idealWorkedTimeForMonth + workDay.getIdealWorkedTime();
			if (workDay.getDay().getDate().isBefore(currentDay)
					|| (workDay.getDay().getDate().equals(currentDay) && workDay.isWorked())) {
				realWorkedTime = realWorkedTime + workDay.getWorkedTime();
				idealWorkedTimeUpToCurrentDay = idealWorkedTimeUpToCurrentDay + workDay.getIdealWorkedTime();
			}
		}
	}

	@Override
	protected void initWorkedTime() {
		if (isWorkedFullMonth()) {
			workedTime = idealWorkedTimeForMonth;
		} else {
			workedTime = isWorkedRequiredTime() ? idealWorkedTimeUpToCurrentDay : realWorkedTime;
		}
	}

	@Override
	protected void initDeltaTime() {
		deltaTime = Math.abs(realWorkedTime - idealWorkedTimeUpToCurrentDay);
	}

	@Override
	protected void initRemainderTime() {
		if (isWorkedFullMonth()) {
			remainderTime = 0;
		} else {
			remainderTime = isWorkedRequiredTime() ? Math.abs(idealWorkedTimeForMonth - realWorkedTime)
					: Math.abs(idealWorkedTimeForMonth - idealWorkedTimeUpToCurrentDay);
		}
	}

	@Override
	protected void initDeltaType() {
		isPositiveDelta = isWorkedRequiredTime();
	}

	private boolean isWorkedRequiredTime() {
		return realWorkedTime > idealWorkedTimeUpToCurrentDay;
	}

}