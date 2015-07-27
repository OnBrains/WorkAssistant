package org.onbrains.entity.worker;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 04.04.2015 18:11.
 */

@Entity
@Table(name = "LOGIN", uniqueConstraints = {@UniqueConstraint(columnNames = {"LOGIN"})})
@NamedQueries
        ({@NamedQuery(name = Login.CHECK_LOGIN,
                        query = "select l.worker from Login l where l.login = :login and l.password = :password"),
          @NamedQuery(name = Login.IS_LOGIN_USED,
                        query = "select 1 from Login l where l.login = :login")})
public class Login implements Serializable {

    public static final String CHECK_LOGIN = "Login.checkLogin";
    public static final String IS_LOGIN_USED = "Login.isLoginUsed";
    public static final Long pref  = 8399978L;

    @Id
    @Column(name = "WORKER_ID", nullable = false)
    private Long workerId;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private Worker worker;

    @Column(name = "LOGIN", nullable = false, length = 32)
    private String login;

    @Column(name = "PASSWORD", nullable = false, length = 512)
    private String password;

    protected Login() {
    }

    public Login(String login, String password, Long workerId) {
        this.login = login;
        this.password = password;
        this.workerId = workerId;
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