package org.onbrains.dao.worker.implementation;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.onbrains.dao.worker.LoginDAOInterface;
import org.onbrains.entity.worker.Login;
import org.onbrains.entity.worker.Worker;
import org.onbrains.utils.encryption.EncryptionService;

/**
 * @author Naumov Oleg on 04.04.2015 19:04.
 */
@Stateless
public class LoginDAO implements LoginDAOInterface {

	@PersistenceContext(unitName = "WA")
	private EntityManager em;

	@Override
	public Worker checkLogin(String loginName, String password) {
		List<Login> loginList = em.createNamedQuery(Login.GET_LOGIN_LIST, Login.class).getResultList();
		for (Login login : loginList) {
			if (login.getLogin().equals(loginName)
					&& login.getPassword().equals(EncryptionService.hash(password, login.getSalt()))) {
				return login.getWorker();
			}
		}
		return null;
	}

	@Override
	public boolean isLoginUsed(String login) {
		long countUsers = (long) em.createNamedQuery(Login.IS_LOGIN_USED).setParameter("login", login).getSingleResult();
		return countUsers != 0;
	}
}