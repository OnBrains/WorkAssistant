package org.onbrains.entity.event;

/**
 * Категории {@linkplain EventType типов событий}, характеризиют влияние {@linkplain Event события} конкретного типа на
 * отработанное время.
 * 
 * @author Naumov Oleg on 01.09.2015 18:32.
 */
public enum EventCategory {

	//@formatter:off
	INFLUENCE_ON_WORKED_TIME        ("Влияет"),
    NOT_INFLUENCE_ON_WORKED_TIME    ("Не влияет"),
    WITH_FIXED_WORKED_TIME          ("С фиксированным временем");
    //@formatter:on

	private String desc;

	private EventCategory(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

}
