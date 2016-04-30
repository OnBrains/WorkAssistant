package org.onbrains.entity.worker;

import org.onbrains.entity.SuperClass;
import org.onbrains.utils.jpa.converter.LocalDateAttributeConverter;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.*;

/**
 * @author Naumov Oleg on 21.03.2015 20:34.
 */

@Entity
@Table(name = "WORKER")
@NamedQuery(name = Worker.GET_ALL_WORKER, query = "select w from Worker w")
public class Worker extends SuperClass {

    public static final String GET_ALL_WORKER = "WorkerDAO.getWorkers";

    @Column(name = "FAMILY", length = 64)
    private String family;

    @Column(name = "FIRST_NAME", length = 32)
    private String firstName;

    @Column(name = "SURNAME", length = 32)
    private String surname;

    @Column(name = "BIRTHDAY")
    @Convert(converter = LocalDateAttributeConverter.class)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEX", length = 16)
    private WorkerSex sex;

    @Column(name = "MOBILE_PHONE", length = 32)
    private String mobilePhone;

    @Column(name = "EMAIL", length = 64)
    private String email;

    public Worker() {
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public WorkerSex getSex() {
        return sex;
    }

    public void setSex(WorkerSex sex) {
        this.sex = sex;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return family + " " + firstName;
    }

}