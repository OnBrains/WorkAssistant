package ru.naumovCorp.entity.worker;

import java.io.Serializable;

import javax.persistence.*;

/**
 * @author Naumov Oleg on 21.03.2015 20:34.
 */

@Entity
@Table(name = "WORKER", uniqueConstraints = {@UniqueConstraint(columnNames = {"FAMILY", "FIRST_NAME", "SURNAME"})})
@NamedQuery(name = Worker.GET_ALL_WORKER, query = "select w from Worker w")
public class Worker implements Serializable {

    public static final String GET_ALL_WORKER = "WorkerDAO.getWorkers";

    @Id
    @GeneratedValue(generator = "WorkerId")
    @SequenceGenerator(name = "WorkerId", sequenceName = "GEN_WORKER_ID", allocationSize = 1)
    @Column(name = "WORKER_ID")
    private Long id;

    @Column(name = "FAMILY", nullable = false, length = 64)
    private String family;

    @Column(name = "FIRST_NAME", nullable = false, length = 32)
    private String firstName;

    @Column(name = "SURNAME", nullable = false, length = 32)
    private String surname;

    protected Worker() {
    }

    public Worker(String family, String firstName, String surname) {
        this.family = family;
        this.firstName = firstName;
        this.surname = surname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    public String toString() {
        return family + " " + firstName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Worker worker = (Worker) o;

        if (id != null ? !id.equals(worker.id) : worker.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}