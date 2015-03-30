package ru.naumovCorp.parsing;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Naumov Oleg on 26.03.2015 23:00.
 */
public class ConvertDate {

    private final static Long msecInHour = 3600000L;
    private final static Long msecInMinutes = 60000L;

    public static String getTime(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(date);
    }

    public static Long convertMSecToHour(Long mSeconds) {
        return mSeconds / msecInHour;
    }

    public static Long convertMSecToMinutes(Long mSeconds) {
        return (mSeconds % msecInHour) / msecInMinutes;
    }

    public static String formattedTimeFromMSec(Long mSeconds) {
        if (mSeconds == null) {
            return null;
        } else if (mSeconds < 0) {
            mSeconds = mSeconds * (-1);
        }
        String hour;
        String minutes;
        if (convertMSecToHour(mSeconds) < 10) {
            hour = "0" + convertMSecToHour(mSeconds);
        } else {
            hour = "" + convertMSecToHour(mSeconds);
        }
        if (convertMSecToMinutes(mSeconds) < 10) {
            minutes = "0" + convertMSecToMinutes(mSeconds);
        } else {
            minutes = "" + convertMSecToMinutes(mSeconds);
        }
        return "" + hour + ":" + minutes;
    }

    public static String dateFormatWithWeekDay(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd EEEEE");
        return simpleDateFormat.format(date);
    }

    public static String dateFormatMonthYear(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMMM yyyy");
        return simpleDateFormat.format(date);
    }

}