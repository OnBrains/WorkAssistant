package org.onbrains.dao.day.implementation;

import org.onbrains.dao.DAOHelper;
import org.onbrains.dao.day.DayDAOInterface;
import org.onbrains.entity.day.Day;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 31.07.2015 0:04.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DayDAO implements DayDAOInterface {

    @Inject
    private DAOHelper dh;

    @Override
    public void create(Day day) {
        dh.persist(day);
    }

    @Override
    public void update(Day day) {
        dh.merge(day);
    }

    @Override
    public void remove(Day day) {
        dh.remove(day);
    }

    @Override
    public Day find(Long dayId) {
        EntityManager em = dh.getEntityManager();
        return em.find(Day.class, dayId);
    }

    @Override
    public List<Day> getDaysByMonth(Date month) {
        EntityManager em = dh.getEntityManager();
        return em.createNamedQuery(Day.GET_DAYS_BY_MONTH, Day.class).setParameter("month", month).getResultList();
    }
}
