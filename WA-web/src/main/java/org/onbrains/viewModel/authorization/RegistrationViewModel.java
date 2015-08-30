package org.onbrains.viewModel.authorization;

import org.onbrains.dao.worker.LoginDAOInterface;
import org.onbrains.dao.worker.WorkerDAOInterface;
import org.onbrains.entity.worker.Login;
import org.onbrains.entity.worker.Worker;
import org.onbrains.entity.worker.WorkerSex;
import org.onbrains.service.SessionUtil;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 19.04.2015 17:53.
 */

@ManagedBean
@RequestScoped
@Transactional
public class RegistrationViewModel implements Serializable {

	@PersistenceContext(unitName = "WA")
	private EntityManager em;
	@Inject
	LoginDAOInterface lDAO;
	@Inject
	WorkerDAOInterface wDAO;

	private String login;
	private String password;
	private String repeatPassword;

	private Worker newWorker = new Worker();

	public WorkerSex[] getWorkerSexs() {
		return WorkerSex.values();
	}

	public String createWorker() {
		if (lDAO.isLoginUsed(login)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Ошибка при регистрации", "Имя пользователя: '" + login + "' занято."));
			return "";
		} else if (!password.equals(repeatPassword)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Ошибка при регистрации", "Неправильное подтверждение пароля"));
			return "";
		} else {
			em.persist(newWorker);
			em.persist(new Login(login, password, newWorker.getId()));
			login();
			return "home";
		}
	}

	private void login() {
		HttpSession session = SessionUtil.getSession();
		session.setAttribute("login", login);
		session.setAttribute("workerId", newWorker.getId());
		session.setAttribute("worker", newWorker);
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
		String hashPassword = Login.pref + password;
		this.password = String.valueOf(hashPassword.hashCode());
	}

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public void setRepeatPassword(String repeatPassword) {
		String hashPassword = Login.pref + repeatPassword;
		this.repeatPassword = String.valueOf(hashPassword.hashCode());
	}

	public Worker getNewWorker() {
		return newWorker;
	}

	public void setNewWorker(Worker newWorker) {
		this.newWorker = newWorker;
	}

}