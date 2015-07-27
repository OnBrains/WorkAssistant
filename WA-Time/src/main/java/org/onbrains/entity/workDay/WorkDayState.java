package org.onbrains.entity.workDay;

/**
 * @author Naumov Oleg on 21.03.2015 22:06.
 */

public enum WorkDayState {

    NO_WORK  ("Не работал", 1),
    WORKING  ("На работе", 2),
//    COMEAWAY ("Отлучился", 3), // пока отлучения не реализованы
    WORKED   ("Отработал", 4);

    private String desc;
    private int order;

    private WorkDayState(String desc, int order) {
         this.desc = desc;
         this.order = order;
    }

    public String getDesc() {
        return desc;
    }

    public int getOrder() {
        return order;
    }

}