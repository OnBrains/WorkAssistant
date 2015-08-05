package org.onbrains.dao;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * @author Naumov Oleg on 21.03.2015 20:34.
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DAOHelper {

    public static EntityManager getEntityManager() {
        return Persistence.createEntityManagerFactory("WA").createEntityManager();
    }

    public void persist(Object object) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(object);
        em.getTransaction().commit();
    }

    public void merge(Object object) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.merge(object);
        em.getTransaction().commit();
    }

    public void remove(Object object) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(object);
        em.getTransaction().commit();
    }

    public Object find(Class<?> clazz, Long id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        Object findObject = em.find(clazz, id);
        em.getTransaction().commit();
        return findObject;
    }

}
