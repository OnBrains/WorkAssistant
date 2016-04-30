package org.onbrains.entity.worker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.onbrains.entity.SuperClass;
import org.onbrains.utils.encryption.EncryptionService;

/**
 * @author Naumov Oleg on 04.04.2015 18:11.
 */

@Entity
@Table(name = "LOGIN", uniqueConstraints = { @UniqueConstraint(columnNames = { "LOGIN" }) })
@NamedQueries({ @NamedQuery(name = Login.GET_LOGIN_LIST, query = "select l from Login l"),
		@NamedQuery(name = Login.IS_LOGIN_USED, query = "select count(*) from Login l where l.login = :login") })
public class Login extends SuperClass {

	public static final String GET_LOGIN_LIST = "Login.getLoginList";
	public static final String IS_LOGIN_USED = "Login.isLoginUsed";

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WORKER_ID")
	private Worker worker;

	@Column(name = "LOGIN", nullable = false, length = 32)
	private String login;

	@Column(name = "PASSWORD", nullable = false, length = 512)
	private String password;

	@Column(name = "SALT", nullable = false, length = 16)
	private String salt;

	protected Login() {
	}

	public Login(String login, String password, Worker worker) {
		this.login = login;
		this.worker = worker;
		this.salt = EncryptionService.salt();
		this.password = EncryptionService.hash(password, this.salt);
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

	public String getSalt() {
		return salt;
	}

	private void setSalt(String salt) {
		this.salt = salt;
	}

}