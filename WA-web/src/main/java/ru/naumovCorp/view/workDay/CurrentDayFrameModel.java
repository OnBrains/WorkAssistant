package ru.naumovCorp.view.workDay;

import ru.naumovCorp.dao.workDay.WorkDayDAOInterface;
import ru.naumovCorp.entity.workDay.WorkDay;
import ru.naumovCorp.entity.workDay.WorkDayState;
import ru.naumovCorp.parsing.ConvertDate;
import ru.naumovCorp.service.SessionUtil;

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
public class CurrentDayFrameModel implements Serializable {

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
     * @return - если день не найден, то "-  "
     */
    public String getComingTime() {
        if (getCurrentDay() != null && isWorking()) {
            return ConvertDate.getTime(currentDay.getComingTime().getTime());
        } else {
            return "-";
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
            return "-";
        }
    }

    /**
     * Актуально для случая, когда рабочий день закончин
     *
     * @return - реальное время ухода
     */
    private String getRealOutTime() {
        return ConvertDate.getTime(currentDay.getOutTime().getTime());
    }

    /**
     * Вычисляет возможное время ухода, в зависимости от времени прихода.
     * Актуально для случая, когда рабочий день не закончин.
     *
     * @return - время прихода + 8.5 часов, если день не найден, то "-"
     */
    private String getPossibleOutTime() {
        if (getCurrentDay() != null) {
            Calendar possibleOutComeTime = Calendar.getInstance();
            possibleOutComeTime.setTimeInMillis(getPossibleOutTimeInMSecond());
            return ConvertDate.getTime(possibleOutComeTime.getTime());
        } else {
            return "-";
        }
    }

    /**
     * @return - возможное время ухода в милисекундах
     */
    private Long getPossibleOutTimeInMSecond() {
        if (getCurrentDay() != null) {
            Long commingTimeInMSeconds = getCurrentDay().getComingTime().getTimeInMillis();
            Long possibleOutTimeInMSecond = commingTimeInMSeconds + WorkDay.mSecondsInWorkDay;
            return possibleOutTimeInMSecond;
        } else {
            return 0L;
        }
    }

    public String getResultWorkedTime() {
        if (isWorking()) {
            if (isWorked()) {
                return ConvertDate.formattedTimeFromMSec(currentDay.getSummaryWorkedTime());
            } else {
                return getTimeForEndWorkDay();
            }
        } else {
            return "-";
        }
    }

    /**
     * Вычисляет оставщееся время до окончания рабочего дня или переработанное время
     *
     * @return - оставшееся/переработанное время, если день не найден, то "-"
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
            return ConvertDate.formattedTimeFromMSec(diffTimeInMSecond);
        } else {
            return "-";
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

}