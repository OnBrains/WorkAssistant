package org.onbrains.utils.parsing;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * @author Naumov Oleg on 26.03.2015 23:00.
 */
@Named(value = "dateFormat")
@SessionScoped
public class DateFormatService implements Serializable {

	private final static Long SEC_IN_HOUR = 3600L;
	private final static Long SEC_IN_MINUTE = 60L;

	/**
	 * Получает из даты информацию о времени.
	 *
	 * @param date
	 *            Преобразуемая дата
	 * @return Информация о дате в формате 'HH:mm'.
	 */
	public static String toHHMM(LocalDateTime date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		return date != null ? formatter.format(date) : null;
	}

	/**
	 * Вычисляет сколько часов есть в переданном количестве миллисекунд.
	 *
	 * @param seconds
	 *            Количество миллисекунд.
	 * @return Количество часов.
	 */
	private static Long secToHour(Long seconds) {
		return seconds / SEC_IN_HOUR;
	}

	/**
	 * Вычисляет сколько минут остается после вычисления часов из переданного количества миллисекунд.
	 *
	 * @param seconds
	 *            Количество миллисекунд.
	 * @return Количество минут.
	 */
	private static Long secToMinutes(Long seconds) {
		return (seconds % SEC_IN_HOUR) / SEC_IN_MINUTE;
	}

	/**
	 * Вычисляет время из миллисекунд.
	 *
	 * @param seconds
	 *            Количество миллисекунд.
	 * @return Время в формате 'HH:mm'.
	 */
	public static String secToHHMM(Long seconds) {
		if (seconds == null) {
			return null;
		}
		Long secondsAbs = Math.abs(seconds);
		String hour;
		String minutes;
		hour = secToHour(secondsAbs) < 10 ? ("0" + secToHour(secondsAbs)) : ("" + secToHour(secondsAbs));
		minutes = secToMinutes(secondsAbs) < 10 ? ("0" + secToMinutes(secondsAbs)) : ("" + secToMinutes(secondsAbs));
		return hour + ":" + minutes;
	}

	/**
	 * Получает из даты информацию о дне месяца и дне недели.
	 *
	 * @param date
	 *            Преобразуемая дата
	 * @return Информация о дате в формате 'dd EE.'.
	 */
	public static String toDDEE(LocalDate date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd EE.");
		return date != null ? formatter.format(date) : null;
	}

	/**
	 * Получает из даты информацию о месяце и годе.
	 *
	 * @param date
	 *            Преобразуемая дата
	 * @return Информация о дате в формате 'MMMMM yyyy'.
	 */
	public static String toMMMMMYYYY(LocalDate date) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendText(ChronoField.MONTH_OF_YEAR, monthMap())
				.appendPattern(" yyyy").toFormatter();
		return date != null ? formatter.format(date) : null;
	}

	/**
	 * Получает из даты информацию о дне недели.
	 *
	 * @param date
	 *            Преобразуемая дата
	 * @return Информация о дате в формате 'EEEEE'.
	 */
	public static String toEEEEE(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEEE", dateFormatSymbols);
		return simpleDateFormat.format(date);
	}

	/**
	 * Получает из даты информацию о дате, месяце и годе.
	 *
	 * @param date
	 *            Преобразуемая дата
	 * @return Информация о дате в формате 'yyyy.MM.dd'.
	 */
	public static String toYYYYMMDD(LocalDateTime date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		return date != null ? formatter.format(date) : null;
	}

	private static Map<Long, String> monthMap() {
		Map<Long, String> monthMap = new HashMap<>();
		monthMap.put(1L, "Январь");
		monthMap.put(2L, "Февраль");
		monthMap.put(3L, "Март");
		monthMap.put(4L, "Апрель");
		monthMap.put(5L, "Май");
		monthMap.put(6L, "Июнь");
		monthMap.put(7L, "Июль");
		monthMap.put(8L, "Август");
		monthMap.put(9L, "Сентябрь");
		monthMap.put(10L, "Октябрь");
		monthMap.put(11L, "Ноябрь");
		monthMap.put(12L, "Декабрь");
		return monthMap;
	}

}