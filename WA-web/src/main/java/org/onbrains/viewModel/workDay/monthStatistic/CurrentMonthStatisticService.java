package org.onbrains.viewModel.workDay.monthStatistic;

import java.util.Calendar;
import java.util.Date;
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
		Calendar currentDay = Calendar.getInstance();
        currentDay.add(Calendar.DATE, -1);
		for (WorkDay workDay : workDays) {
			idealWorkedTimeForMonth = idealWorkedTimeForMonth + workDay.getIdealWorkedTime();
			if (workDay.getDay().getDay().before(currentDay.getTime())) {
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