package org.onbrains.viewModel.workDay;

import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.workDay.WorkDayState;
import org.onbrains.utils.parsing.DateFormatService;
import org.onbrains.service.SessionUtil;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Naumov Oleg on 05.04.2015 13:57.
 */
@ManagedBean
@ViewScoped
public class CurrentDayFrameModelOld implements Serializable {

    @Inject
    private WorkDayDAOInterface wdDAO;
    @Inject
    private SessionUtil sUtil;

    private WorkDay currentDay;

    public WorkDay getCurrentDay() {
        if (currentDay == null) {
            currentDay = wdDAO.getCurrentDayInfo(new Date(), sUtil.getWorker());
        }
        return currentDay;
    }

    /**
     * Проставляет время прихода для текущего дня, проставляется текущее время
     */
    public void startWork() {
        currentDay.setComingTime(Calendar.getInstance());
        currentDay.setState(WorkDayState.WORKING);
        wdDAO.update(currentDay);
    }

    /**
     * Проставляет время ухода для текущего дня, проставляется текущее время
     */
    public void endWork() {
        currentDay.setOutTime(Calendar.getInstance());
        currentDay.setState(WorkDayState.WORKED);
        wdDAO.update(currentDay);
    }

    public boolean isWorking() {
        if (getCurrentDay() != null) {
            return !currentDay.getState().equals(WorkDayState.NO_WORK);
        } else {
            return false;
        }
    }

    public boolean isWorked() {
        if (getCurrentDay() != null) {
            return currentDay.getState().equals(WorkDayState.WORKED);
        } else {
            return false;
        }
    }

    /**
     * Возвращает отформатированное время прихода, для текущего дня
     *
     * @return - если день не найден, то "__:__"
     */
    public String getComingTime() {
        if (getCurrentDay() != null && isWorking()) {
            return DateFormatService.toHHMM(currentDay.getComingTime().getTime());
        } else {
            return "__:__";
        }
    }

    public String getOutTime() {
        if (isWorking()) {
            if (isWorked()) {
                return getRealOutTime();
            } else {
                return getPossibleOutTime();
            }
        } else {
            return "__:__";
        }
    }

    /**
     * Актуально для случая, когда рабочий день закончин
     *
     * @return - реальное время ухода
     */
    private String getRealOutTime() {
        return DateFormatService.toHHMM(currentDay.getOutTime().getTime());
    }

    /**
     * Вычисляет возможное время ухода, в зависимости от времени прихода.
     * Актуально для случая, когда рабочий день не закончин.
     *
     * @return - время прихода + время в зависимости от типа дня, если день не найден, то "__:__"
     */
    private String getPossibleOutTime() {
        if (getCurrentDay() != null) {
            Calendar possibleOutComeTime = Calendar.getInstance();
            possibleOutComeTime.setTimeInMillis(getPossibleOutTimeInMSecond());
            return DateFormatService.toHHMM(possibleOutComeTime.getTime());
        } else {
            return "__:__";
        }
    }

    /**
     * @return - возможное время ухода в милисекундах
     */
    private Long getPossibleOutTimeInMSecond() {
        if (getCurrentDay() != null) {
            Long commingTimeInMSeconds = getCurrentDay().getComingTime().getTimeInMillis();
            Long possibleOutTimeInMSecond = commingTimeInMSeconds + currentDay.getDay().getType().getWorkTimeInMSecond();
            return possibleOutTimeInMSecond;
        } else {
            return 0L;
        }
    }

    public String getCurrentWorkedTime() {
        if (isWorking()) {
            if (isWorked()) {
                return DateFormatService.mSecToHHMM(currentDay.getSummaryWorkedTime());
            } else {
                Calendar currentTime = Calendar.getInstance();
                return DateFormatService.mSecToHHMM(currentTime.getTimeInMillis() - currentDay.getComingTime().getTimeInMillis());
            }
        } else {
            return "__:__";
        }
    }

    public String getResultWorkedTime() {
        if (isWorking()) {
            if (isWorked()) {
                return DateFormatService.mSecToHHMM(currentDay.getDeltaTime());
            } else {
                return getTimeForEndWorkDay();
            }
        } else {
            return "__:__";
        }
    }

    /**
     * Вычисляет оставщееся время до окончания рабочего дня или переработанное время
     *
     * @return - оставшееся/переработанное время, если день не найден, то "__:__"
     */
    private String getTimeForEndWorkDay() {
        if (getCurrentDay() != null) {
            Calendar diffTime = Calendar.getInstance();
            Long possibleOutTime = getPossibleOutTimeInMSecond();
            Long diffTimeInMSecond;
            if (!ifCurrentTimeMoreOutTime()) {
                diffTimeInMSecond = possibleOutTime - diffTime.getTimeInMillis();
            } else {
                diffTimeInMSecond = diffTime.getTimeInMillis() - possibleOutTime;
            }
            return DateFormatService.mSecToHHMM(diffTimeInMSecond);
        } else {
            return "__:__";
        }
    }

    /**
     * Сравнивает текущее время с возможным временем ухода для текущего дня
     *
     * @return - true если текущее время больше
     */
    public boolean ifCurrentTimeMoreOutTime() {
        Calendar currentTime = Calendar.getInstance();
        if (getCurrentDay() != null && currentTime.getTimeInMillis() > getPossibleOutTimeInMSecond()) {
            return true;
        } else {
            return false;
        }
    }

    public String getLegendValue() {
        return getCurrentDay() != null ? DateFormatService.toDDEE(currentDay.getDay().getDay()) +
                " - " + currentDay.getState().getDesc() : "Не найдено";
    }

}