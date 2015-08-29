package org.onbrains.entity.workDay;

/**
 * @author Naumov Oleg on 11.04.2015 13:07.
 *
 *         Типы дней, пока что видится только три типа:
 *         <ul>
 *         <li>Рабочий день</li>
 *         <li>Выходной</li>
 *         <li>Сокращенный день</li>
 *         </ul>
 *         Для типа дня можно задать {@linkplain #getWorkTimeInMSecond() время}, которое необходимо отработать и
 *         {@linkplain #getPathToIcon() иконку}
 */
public enum DayType {

	WORK_DAY("Рабочий день", 30600000L, ""), HOLIDAY("Выходной", 0L, ""), SHORT_WORK_DAY("Сокращенный день", 27000000L,
			"img/dayType/short_work_day.png");

	private String desc;
	private Long workTimeInMSecond;
	private String pathToIcon;

	private DayType(String desc, Long workTimeInMSecond, String pathToIcon) {
		this.desc = desc;
		this.workTimeInMSecond = workTimeInMSecond;
		this.pathToIcon = pathToIcon;
	}

	/**
	 * Понятное наименование типа рабочего дня, которое можно отобразить в UI.
	 *
	 * @return Наименование типа рабочего дня.
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @return Время, которое необходимо отработать в рабочий день данного типа.
	 */
	public Long getWorkTimeInMSecond() {
		return workTimeInMSecond;
	}

	/**
	 * @return Путь до места, в котором лежит иконта для данного типа рабочего дня.
	 */
	public String getPathToIcon() {
		return pathToIcon;
	}

}