package ru.naumovCorp.entity.workDay;

/**
 * @author Naumov Oleg on 11.04.2015 13:07.
 */

public enum DayType {

    WORK_DAY       ("Рабочий день", 30600000L),
    HOLIDAY        ("Выходной", 0L),
    DAY_OFF        ("Отгул", 0L),
    SHORT_WORK_DAY ("Сокращенный день", 27000000L);

    private String desc;
    private Long workTimeInMSecond;

    private DayType(String desc, Long workTimeInMSecond) {
        this.desc = desc;
        this.workTimeInMSecond = workTimeInMSecond;
    }

    public String getDesc() {
        return desc;
    }

    public Long getWorkTimeInMSecond() {
        return workTimeInMSecond;
    }
}