package org.onbrains.utils.parsing;

import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Naumov Oleg on 26.03.2015 23:00.
 */
@ManagedBean(name = "dateFormat")
@SessionScoped
public class DateFormatService implements Serializable {

    private final static Long mSecInHour = 3600000L;
    private final static Long mSecInMinutes = 60000L;

    /**
     * Получает из даты информацию о времени.
     *
     * @param date Преобразуемая дата
     * @return Информация о дате в формате 'HH:mm'.
     */
    public static String toHHMM(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(date);
    }

    /**
     * Вычисляет сколько часов есть в переданном количестве миллисекунд.
     *
     * @param mSeconds Количество миллисекунд.
     * @return Количество часов.
     */
    public static Long mSecToHour(Long mSeconds) {
        return mSeconds / mSecInHour;
    }

    /**
     * Вычисляет сколько минут остается после вычисления часов из переданного количества миллисекунд.
     *
     * @param mSeconds Количество миллисекунд.
     * @return Количество минут.
     */
    public static Long mSecToMinutes(Long mSeconds) {
        return (mSeconds % mSecInHour) / mSecInMinutes;
    }

    /**
     * Вычисляет время из миллисекунд.
     *
     * @param mSeconds Количество миллисекунд.
     * @return Время в формате 'HH:mm'.
     */
    public static String mSecToHHMM(Long mSeconds) {
        if (mSeconds == null) {
            return null;
        }
        Long mSecondsAbs = Math.abs(mSeconds);
        String hour;
        String minutes;
        hour = mSecToHour(mSecondsAbs) < 10 ? ("0" + mSecToHour(mSecondsAbs)) : ("" + mSecToHour(mSecondsAbs));
        minutes = mSecToMinutes(mSecondsAbs) < 10 ? ("0" + mSecToMinutes(mSecondsAbs)) : ("" + mSecToMinutes(mSecondsAbs));
        return hour + ":" + minutes;
    }

    /**
     * Получает из даты информацию о дне месяца и дне недели.
     *
     * @param date Преобразуемая дата
     * @return Информация о дате в формате 'dd EE.'.
     */
    public static String toDDEE(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd EE.");
        return simpleDateFormat.format(date);
    }

    /**
     * Получает из даты информацию о месяце и годе.
     *
     * @param date Преобразуемая дата
     * @return Информация о дате в формате 'MMMMM yyyy'.
     */
    public static String toMMMMMYYYY(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMMM yyyy", dateFormatSymbols);
        return simpleDateFormat.format(date);
    }

    /**
     * Получает из даты информацию о дате, месяце и годе.
     *
     * @param date Преобразуемая дата
     * @return Информация о дате в формате 'yyyy.MM.dd'.
     */
    public static String toYYYYMMDD(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        return simpleDateFormat.format(date);
    }

    private static DateFormatSymbols dateFormatSymbols = new DateFormatSymbols() {

        @Override
        public String[] getMonths() {
            return new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        }

    };

}