package org.onbrains.dao.worker.implementation;

import org.onbrains.dao.DAOHelper;
import org.onbrains.dao.worker.LoginDAOInterface;
import org.onbrains.entity.worker.Login;
import org.onbrains.entity.worker.Worker;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * @author Naumov Oleg on 04.04.2015 19:04.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class LoginDAO implements LoginDAOInterface {

    @Inject
    private DAOHelper dh;

    @Override
    public void create(Login login) {
        dh.persist(login);
    }

    @Override
    public void update(Login login) {
        dh.merge(login);
    }

    @Override
    public void remove(Login login) {
        dh.remove(login);
    }

    @Override
    public Worker checkLogin(String login, String password) {
        EntityManager em = dh.getEntityManager();
        try {
            Worker worker = em.createNamedQuery(Login.CHECK_LOGIN, Worker.class)
                    .setParameter("login", login)
                    .setParameter("password", password).getSingleResult();
            return worker;
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public boolean isLoginUsed(String login) {
        EntityManager em = dh.getEntityManager();
        try {
            int isUsed = (int) em.createNamedQuery(Login.IS_LOGIN_USED)
                    .setParameter("login", login).getSingleResult();
            return isUsed == 1;
        } catch (NoResultException ex) {
            return false;
        }
    }
}