package org.onbrains.component.statistic;

import java.text.DecimalFormat;

/**
 * Класс описывающий значение, которое надо отобразить в {@linkplain StatisticModel компоненте статистики}. Например:
 * <ul>
 * <li>Отработаное время за день;</li>
 * <li>Оставшееся/переработаное время за день.</li>
 * </ul>
 * <p/>
 * Created by Naumov Oleg on 03.01.2016.
 */
public class StatisticValue {

	private final static Long mSecInHour = 3600000L;
	private final static Long mSecInMinutes = 60000L;

	private long count;
	private String title;
	private String color;
	private float percentage;

	public StatisticValue(long count, String title, String color) {
		this.count = count;
		this.title = title;
		this.color = color;
	}

	/**
	 * Возвращает значение количества в определенном формате. Возможные форматы:
	 * <ul>
	 * <li><strong>time</strong> - формат времени "hh:mm"</li>
	 * <li><strong>percentage</strong> - формат процентов</li>
	 * </ul>
	 * 
	 * @param valueType
	 *            тип необходимого формата.
	 * @return Отформатированное значение количества.
	 */
	public String getValue(String valueType) {
		switch (valueType) {
		case "time":
			return getTimeValue();
		case "percentage":
			return getPercentageValue();
		default:
			return "";
		}
	}

	/**
	 * @return Строковое значение количества в формате процентов.
	 */
	public String getPercentageValue() {
		DecimalFormat df = new DecimalFormat("##.##");
		return String.format("%s%s", df.format(percentage), "%");
	}

	/**
	 * Преобразует {@linkplain #getValue количество} в формат времени "hh:mm".
	 * 
	 * @return Строковое значение количества в формате времени.
	 */
	private String getTimeValue() {
		if (count == 0) {
			return "00:00";
		}
		Long mSecondsAbs = Math.abs(count);
		String hour;
		String minutes;
		long hoursFromCount = mSecondsAbs / mSecInHour;
		long minutesFromCount = (mSecondsAbs % mSecInHour) / mSecInMinutes;
		hour = hoursFromCount < 10 ? "0" + hoursFromCount : "" + hoursFromCount;
		minutes = minutesFromCount < 10 ? "0" + minutesFromCount : "" + minutesFromCount;
		return hour + ":" + minutes;
	}

	/**
	 * @return Количество.
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @return Заголовок, который будет отображен в UI.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Значение цвета, для легенды данного значения.
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Вычисляется в {@linkplain StatisticModel#calculatePercentageFor модели компонента}.
	 * 
	 * @return Процентная часть, для данного значения.
	 */
	public float getPercentage() {
		return percentage;
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}
}