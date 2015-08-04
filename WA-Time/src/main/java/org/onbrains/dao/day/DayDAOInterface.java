package org.onbrains.dao.day;

import org.onbrains.entity.day.Day;

import java.util.Date;
import java.util.List;

/**
 * @author Naumov Oleg on 31.07.2015 0:01.
 */
public interface DayDAOInterface {

    public void create(Day day);

    public void update(Day day);

    public void remove(Day day);

    public Day find(Long dayId);

    public List<Day> getDaysByMonth(Date month);
}
