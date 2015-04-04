package ru.naumovCorp.entity.worker;

import ru.naumovCorp.entity.worker.Worker;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 04.04.2015 18:11.
 */

@Entity
@Table(name = "LOGIN", uniqueConstraints = {@UniqueConstraint(columnNames = {"LOGIN"})})
@NamedQueries
        ({@NamedQuery(name = Login.CHECK_LOGIN,
                        query = "select l.worker from Login l where l.login = :login and l.password = :password")})
public class Login implements Serializable {

    public static final String CHECK_LOGIN = "Login.checkLogin";

    @Id
    @Column(name = "WORKER_ID", nullable = false)
    private Long workerId;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private Worker worker;

    @Column(name = "LOGIN", nullable = false)
    private String login;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    protected Login() {
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}