package org.onbrains.entity.worker;

import org.onbrains.entity.SuperClass;

import java.io.Serializable;
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

    @Column(name = "FAMILY", nullable = false, length = 64)
    private String family;

    @Column(name = "FIRST_NAME", nullable = false, length = 32)
    private String firstName;

    @Column(name = "SURNAME", nullable = true, length = 32)
    private String surname;

    @Column(name = "BIRTHDAY", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEX", nullable = false, length = 16)
    private WorkerSex sex;

    @Column(name = "MOBILE_PHONE", nullable = true, length = 32)
    private String modilePhone;

    @Column(name = "EMAIL", nullable = false, length = 64)
    private String email;

    public Worker() {
    }

    public Worker(String family, String firstName, String surname, Date birthday, WorkerSex sex, String email) {
        this.family = family;
        this.firstName = firstName;
        this.surname = surname;
        this.birthday = birthday;
        this.sex = sex;
        this.email = email;
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

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public WorkerSex getSex() {
        return sex;
    }

    public void setSex(WorkerSex sex) {
        this.sex = sex;
    }

    public String getModilePhone() {
        return modilePhone;
    }

    public void setModilePhone(String modilePhone) {
        this.modilePhone = modilePhone;
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