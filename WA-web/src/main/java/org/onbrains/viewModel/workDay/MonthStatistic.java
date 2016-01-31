package org.onbrains.viewModel.workDay;

import org.onbrains.entity.workDay.WorkDay;

import javax.ejb.Stateful;
import java.io.Serializable;
import java.util.List;

/**
 * @author Naumov Oleg on 17.04.2015 22:01.
 */

@Stateful
public class MonthStatistic implements Serializable {

	private long idealWorkedTimeByAllMonth;
	private long idealWorkedTimeByCurrentDay;
	private long realWorkedTime;
	private long workedTime;
	private long deltaTime;
	private long remainingTime;

	public boolean isRealWorkedTimeMoreIdeal() {
		return realWorkedTime > idealWorkedTimeByCurrentDay;
	}

	private void cleanSummeryTimes() {
		idealWorkedTimeByAllMonth = 0L;
		idealWorkedTimeByCurrentDay = 0L;
		realWorkedTime = 0L;
	}

	private void calculateSummaryTime(List<WorkDay> workDays, List<WorkDay> workDaysByMonthType) {
		cleanSummeryTimes();
		if (workDaysByMonthType != null) {
			for (WorkDay wd : workDaysByMonthType) {
				realWorkedTime = realWorkedTime + wd.getWorkingTime();
				idealWorkedTimeByCurrentDay = idealWorkedTimeByCurrentDay
						+ wd.getDay().getType().getWorkTimeInMSecond();
			}
			if (workDaysByMonthType.equals(workDays)) {
				idealWorkedTimeByAllMonth = idealWorkedTimeByCurrentDay;
			} else {
				for (WorkDay wd : workDays) {
					idealWorkedTimeByAllMonth = idealWorkedTimeByAllMonth
							+ wd.getDay().getType().getWorkTimeInMSecond();
				}
			}
		}
	}

	private void calculateDeltaTime() {
		deltaTime = isRealWorkedTimeMoreIdeal() ? realWorkedTime - idealWorkedTimeByCurrentDay
				: idealWorkedTimeByCurrentDay - realWorkedTime;
	}

	private void calculateWorkedTime() {
		workedTime = isRealWorkedTimeMoreIdeal() ? idealWorkedTimeByCurrentDay
				: idealWorkedTimeByCurrentDay - deltaTime;
	}

	private void calculateRemainingTime() {
		if (realWorkedTime < idealWorkedTimeByAllMonth) {
			remainingTime = isRealWorkedTimeMoreIdeal() ? idealWorkedTimeByAllMonth - realWorkedTime
					: idealWorkedTimeByAllMonth - realWorkedTime - deltaTime;
		} else {
			remainingTime = 0;
		}
	}

	public long getSummaryTime() {
		return workedTime + deltaTime + remainingTime;
	}

	public void calculateStatistic(List<WorkDay> workDays, List<WorkDay> workDaysByType) {
		calculateSummaryTime(workDays, workDaysByType);
		calculateDeltaTime();
		calculateWorkedTime();
		calculateRemainingTime();
	}

	public long getWorkedTime() {
		return workedTime;
	}

	public long getDeltaTime() {
		return deltaTime;
	}

	public long getRemainingTime() {
		return remainingTime;
	}

}