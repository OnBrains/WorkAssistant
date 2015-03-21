package ru.naumovCorp.entity.worker;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author Naumov Oleg on 21.03.2015 20:34.
 */

@Entity
@Table(name = "WORKER", uniqueConstraints = {@UniqueConstraint(columnNames = {"FAMILY", "FIRSTNAME", "SURNAME"})})
public class Worker implements Serializable {

    @Id
    @GeneratedValue(generator = "WorkerId")
    @SequenceGenerator(name = "WorkerId", sequenceName = "GEN_WORKER_ID", allocationSize = 1)
    @Column(name = "WORKER_ID")
    private Long workerId;

    @Column(name = "FAMILY", nullable = false, length = 64)
    private String family;

    @Column(name = "FIRSTNAME", nullable = false, length = 32)
    private String firstName;

    @Column(name = "SURNAME", nullable = false, length = 32)
    private String surname;

    public Worker() {
    }

    public Worker(String family, String firstName, String surname) {
        this.family = family;
        this.firstName = firstName;
        this.surname = surname;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Worker worker = (Worker) o;

        if (workerId != null ? !workerId.equals(worker.workerId) : worker.workerId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return workerId != null ? workerId.hashCode() : 0;
    }

}