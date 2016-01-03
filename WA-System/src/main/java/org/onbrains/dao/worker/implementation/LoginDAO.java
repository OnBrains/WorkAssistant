package org.onbrains.dao.worker.implementation;

import org.onbrains.dao.worker.LoginDAOInterface;
import org.onbrains.entity.worker.Login;
import org.onbrains.entity.worker.Worker;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * @author Naumov Oleg on 04.04.2015 19:04.
 */

@Stateless
public class LoginDAO implements LoginDAOInterface {

    @PersistenceContext(unitName = "WA")
    private EntityManager em;

    @Override
    public Worker checkLogin(String login, String password) {
        try {
            return em.createNamedQuery(Login.CHECK_LOGIN, Worker.class)
                    .setParameter("login", login)
                    .setParameter("password", password).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public boolean isLoginUsed(String login) {
        try {
            int isUsed = (int) em.createNamedQuery(Login.IS_LOGIN_USED)
                    .setParameter("login", login).getSingleResult();
            return isUsed == 1;
        } catch (NoResultException ex) {
            return false;
        }
    }
}