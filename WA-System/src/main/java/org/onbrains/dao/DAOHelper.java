package org.onbrains.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * @author Naumov Oleg on 21.03.2015 20:34.
 */

@Stateless
public class DAOHelper {

    public static EntityManager getEntityManager() {
        return Persistence.createEntityManagerFactory("WA").createEntityManager();
    }

    public static void persist(Object object) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(object);
        em.getTransaction().commit();
    }

    public static void merge(Object object) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.merge(object);
        em.getTransaction().commit();
    }

    public static void remove(Object object) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(object);
        em.getTransaction().commit();
    }

}
