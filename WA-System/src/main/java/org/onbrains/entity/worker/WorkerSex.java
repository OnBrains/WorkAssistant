package org.onbrains.entity.worker;

/**
 * @author Naumov Oleg on 19.04.2015 18:34.
 */

public enum WorkerSex {

    MAN     ("Мужской"),
    WOMAN   ("Женский");

    private String desc;

    private WorkerSex(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}