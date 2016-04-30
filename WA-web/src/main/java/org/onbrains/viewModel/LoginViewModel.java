package org.onbrains.viewModel;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.onbrains.dao.worker.LoginDAOInterface;
import org.onbrains.entity.worker.Worker;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.information.Notification;

/**
 * @author Naumov Oleg on 04.04.2015 19:54.
 */
@ManagedBean
@SessionScoped
public class LoginViewModel implements Serializable {

	private static final long serialVersionUID = 2505422498261747842L;

	@Inject
	private LoginDAOInterface ld;

	private String login;
	private String password;

	public String checkLogin() {
		Worker worker = ld.checkLogin(login, password);
		if (worker != null) {
			HttpSession session = SessionUtil.getSession();
			session.setAttribute("login", login);
			session.setAttribute("workerId", worker.getId());
			session.setAttribute("worker", worker);
			return "home";
		} else {
			Notification.info("Ошибка при авторизации", "Повторите попытку входа");
			return "login";
		}
	}

	public String logout() {
		HttpSession session = SessionUtil.getSession();
		session.invalidate();
		return "login";
	}

	public Worker getWorker() {
		return SessionUtil.getWorker();
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

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