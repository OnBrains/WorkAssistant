package ru.naumovCorp.view.authorization;

import org.primefaces.context.RequestContext;
import ru.naumovCorp.dao.worker.LoginDAOInterface;
import ru.naumovCorp.dao.worker.WorkerDAOInterface;
import ru.naumovCorp.entity.worker.Login;
import ru.naumovCorp.entity.worker.Worker;
import ru.naumovCorp.entity.worker.WorkerSex;
import ru.naumovCorp.service.SessionUtil;
import ru.naumovCorp.view.LoginViewModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 19.04.2015 17:53.
 */

@ManagedBean
@RequestScoped
public class RegistrationViewModel implements Serializable {

    @Inject
    LoginDAOInterface lDAO;
    @Inject
    WorkerDAOInterface wDAO;

    private String login;
    private String password;

    private Worker newWorker = new Worker();

    public WorkerSex[] getWorkerSexs() {
        return WorkerSex.values();
    }

    public String createWorker() {
        if (lDAO.isLoginUsed(login)) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Ошибка при регистрации",
                            "Имя пользователя: '" + login + "' занято."));
            return "";
        } else {
            wDAO.create(newWorker);
            lDAO.create(new Login(login, password, newWorker.getId()));
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
        this.password = password;
    }

    public Worker getNewWorker() {
        return newWorker;
    }

    public void setNewWorker(Worker newWorker) {
        this.newWorker = newWorker;
    }

}