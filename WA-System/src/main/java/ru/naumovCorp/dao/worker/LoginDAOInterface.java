package ru.naumovCorp.dao.worker;

import ru.naumovCorp.entity.worker.Worker;

/**
 * @author Naumov Oleg on 04.04.2015 18:53.
 */
public interface LoginDAOInterface {

    public Worker checkLogin(String login, String password);

}