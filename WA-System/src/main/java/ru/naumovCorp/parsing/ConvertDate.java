package ru.naumovCorp.parsing;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Naumov Oleg on 06.04.2015 23:00.
 */
public class ConvertDate {

    public static String getTime(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(date);
    }

}
