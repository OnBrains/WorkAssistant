package org.onbrains.entity.event;

/**
 * @author Naumov Oleg on 09.09.2015 21:03.
 */
public enum EventState {

    //@formatter:off
    END        ("Закончено"),
    NOT_END    ("Не закончено");
    //@formatter:on

    private String desc;

    private EventState(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
