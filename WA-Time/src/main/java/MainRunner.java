import org.onbrains.dao.day.DayDAOInterface;
import org.onbrains.dao.day.implementation.DayDAO;
import org.onbrains.entity.day.Day;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Naumov Oleg on 01.08.2015 10:22.
 */
public class MainRunner {

    public static final void main(String[] args) {
        DayDAOInterface dDAO = new DayDAO();
        List<Day> daysBySelectedMonth = new ArrayList<>();
        daysBySelectedMonth = dDAO.getDaysByMonth(LocalDate.now());
        for (Day day: daysBySelectedMonth) {
            System.out.println(day.getDay());
        }
    }

}
