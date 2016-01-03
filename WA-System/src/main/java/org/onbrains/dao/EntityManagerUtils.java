package org.onbrains.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

/**
 * Локальный <strong>EntityManager</strong>, который можно использовать не в <strong>EJB</strong> бинах, для выполнения
 * <strong>CRUD</strong> операций.
 * <p/>
 * Created by Oleg Naumov on 16.12.2015.
 */
@Stateless
public class EntityManagerUtils implements Serializable {

	@PersistenceContext(unitName = "WA")
	private EntityManager entityManager;

	public void persist(Object object) {
		entityManager.persist(object);
	}

	public <T> T merge(T t) {
		return entityManager.merge(t);
	}

	public void refresh(Object object) {
		entityManager.refresh(object);
	}

	public void remove(Object object) {
		if (!entityManager.contains(object)) {
			object = entityManager.merge(object);
		}
		entityManager.remove(object);
	}

	public <T> T find(Class<T> clazz, Object object) {
		return entityManager.find(clazz, object);
	}

}
