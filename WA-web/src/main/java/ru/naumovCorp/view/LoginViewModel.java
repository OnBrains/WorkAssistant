package ru.naumovCorp.view;

import ru.naumovCorp.SessionUtil;
import ru.naumovCorp.dao.worker.LoginDAOInterface;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 04.04.2015 19:54.
 */

@ManagedBean
@SessionScoped
public class LoginViewModel implements Serializable {

    @Inject
    private LoginDAOInterface ld;

    private String login;
    private String password;

    public String checkLogin() {
        boolean result = ld.checkLogin(login, password);
        if (result) {
            HttpSession session = SessionUtil.getSession();
            session.setAttribute("login", login);
            return "home";
        } else {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Invalid Login!",
                            "Please Try Again!"));
            return "login";
        }
    }

    public String logout() {
        HttpSession session = SessionUtil.getSession();
        session.invalidate();
        return "login";
    }

    /**
     * ****************************************************************************************************************
     * Simple getters and setters
     * ****************************************************************************************************************
     */

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