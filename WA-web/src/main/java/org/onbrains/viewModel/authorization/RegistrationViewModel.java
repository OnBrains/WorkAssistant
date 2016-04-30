package org.onbrains.viewModel.authorization;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.dao.day.DayDAOInterface;
import org.onbrains.dao.worker.LoginDAOInterface;
import org.onbrains.entity.day.Day;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.entity.worker.Login;
import org.onbrains.entity.worker.Worker;
import org.onbrains.service.SessionUtil;

/**
 * @author Naumov Oleg on 19.04.2015 17:53.
 */
@Named
@RequestScoped
public class RegistrationViewModel implements Serializable {

	private static final long serialVersionUID = 1424337630727020939L;

	@Inject
	private EntityManagerUtils em;
	@Inject
	LoginDAOInterface lDAO;
	@Inject
	private DayDAOInterface dDAO;

	private String login;
	private String password;
	private String repeatPassword;

	private Worker newWorker = new Worker();

	public String createWorker() {
		if (lDAO.isLoginUsed(login)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Ошибка при регистрации", "Имя пользователя: '" + login + "' занято."));
			return "";
		} else if (!password.equals(repeatPassword)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Ошибка при регистрации", "Неправильное подтверждение пароля"));
			return "";
		}
		newWorker.setFirstName(login);
		em.persist(newWorker);
		em.persist(new Login(login, password, newWorker));
		createWorkDays();
		login();
		return "home";
	}

	// *****************************************************************************************************************
	// Private methods
	// *****************************************************************************************************************

	private void login() {
		HttpSession session = SessionUtil.getSession();
		session.setAttribute("login", login);
		session.setAttribute("workerId", newWorker.getId());
		session.setAttribute("worker", newWorker);
	}

	private void createWorkDays() {
		List<Day> daysBySelectedMonth = dDAO.getDaysByMonth(LocalDate.now());
		for (Day day : daysBySelectedMonth) {
			em.persist(new WorkDay(newWorker, day));
		}
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

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}

}