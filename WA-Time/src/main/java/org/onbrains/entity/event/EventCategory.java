package org.onbrains.entity.event;

/**
 * @author Naumov Oleg on 01.09.2015 18:32.
 */
public enum EventCategory {

	//@formatter:off
	INFLUENCE_ON_WORKED_TIME        ("Влияет на отработанное время"),
    NOT_INFLUENCE_ON_WORKED_TIME    ("Не влияет на отработанное время"),
    WITH_FIXED_WORKED_TIME          ("С фиксированным отработанным временем");
    //@formatter:on

	private String desc;

	private EventCategory(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

}
