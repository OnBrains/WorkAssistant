package ru.naumovCorp.entity.workDay;

/**
 * @author Naumov Oleg on 11.04.2015 13:07.
 */

public enum DayType {

    WORK_DAY       ("Рабочий день", 30600000L, ""),
    HOLIDAY        ("Выходной", 0L, ""),
    DAY_OFF        ("Отгул", 30600000L, "img/dayType/day_off.png"),
    SHORT_WORK_DAY ("Сокращенный день", 27000000L, "img/dayType/short_work_day.png"),
    BUSINESS_TRIP  ("Командировка", 0L, "img/dayType/business_trip.png"),
    HOSPITAL       ("Больничный", 0L, "img/dayType/hospital.png"),
    VACATION       ("Отпуск", 0L, "img/dayType/vacation.png");

    private String desc;
    private Long workTimeInMSecond;
    private String icon;

    private DayType(String desc, Long workTimeInMSecond, String icon) {
        this.desc = desc;
        this.workTimeInMSecond = workTimeInMSecond;
        this.icon = icon;
    }

    public String getDesc() {
        return desc;
    }

    public Long getWorkTimeInMSecond() {
        return workTimeInMSecond;
    }

    public String getIcon() {
        return icon;
    }

}