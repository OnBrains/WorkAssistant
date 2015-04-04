package ru.naumovCorp.dao.worker.implementation;

import ru.naumovCorp.dao.DAOHelper;
import ru.naumovCorp.dao.worker.LoginDAOInterface;
import ru.naumovCorp.entity.worker.Login;
import ru.naumovCorp.entity.worker.Worker;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * @author Naumov Oleg on 04.04.2015 19:04.
 */

@Stateless
public class LoginDAO implements LoginDAOInterface {

    @Inject
    private DAOHelper dh;

    @Override
    public boolean checkLogin(String login, String password) {
        EntityManager em = dh.getEntityManager();
        try {
            Worker worker = em.createNamedQuery(Login.CHECK_LOGIN, Worker.class)
                    .setParameter("login", login)
                    .setParameter("password", password).getSingleResult();
            return true;
        } catch (NoResultException ex) {
            return false;
        }
    }

}