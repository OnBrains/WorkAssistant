package ru.naumovCorp.dao.worker;

/**
 * @author Naumov Oleg on 04.04.2015 18:53.
 */
public interface LoginDAOInterface {

    public boolean checkLogin(String login, String password);

}