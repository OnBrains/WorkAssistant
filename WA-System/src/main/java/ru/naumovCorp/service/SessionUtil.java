package ru.naumovCorp.service;

import javax.ejb.Stateless;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Naumov Oleg on 04.04.2015 19:39.
 */

@Stateless
public class SessionUtil {

    public static HttpSession getSession() {
        return (HttpSession)
                FacesContext.
                        getCurrentInstance().
                        getExternalContext().
                        getSession(false);
    }

    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.
                getCurrentInstance().
                getExternalContext().getRequest();
    }

    public static String getLogin() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        return session.getAttribute("login").toString();
    }

    public static Long getWorkerId() {
        HttpSession session = getSession();
        if (session != null)
            return (Long) session.getAttribute("workerId");
        else
            return null;
    }
}
