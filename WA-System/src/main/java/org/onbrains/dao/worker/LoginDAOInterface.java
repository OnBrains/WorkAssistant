package org.onbrains.dao.worker;

import org.onbrains.entity.worker.Login;
import org.onbrains.entity.worker.Worker;

/**
 * @author Naumov Oleg on 04.04.2015 18:53.
 */
public interface LoginDAOInterface {

    public void create(Login login);
    public void update(Login login);
    public void remove(Login login);
    public Worker checkLogin(String login, String password);
    public boolean isLoginUsed(String login);

}