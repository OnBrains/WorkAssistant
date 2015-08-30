package org.onbrains.viewModel.event;

import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.EventType;
import org.primefaces.event.RowEditEvent;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Naumov Oleg on 30.08.2015 15:30.
 */

@Named
@SessionScoped
@Transactional
public class EventTypesViewModel implements Serializable {

	private static final long MILLIS_IN_MINUTE = 60000;
	private static final long MILLIS_IN_HOUR = 3600000;

	@PersistenceContext(unitName = "WA")
	private EntityManager em;
	@Inject
	private EventTypeDAOInterface etDAO;

	private long selectedNoWorkHour = 0;
	private long selectedNoWorkMinute = 0;

	public void onRowEdit(RowEditEvent event) {
		EventType editionEventType = (EventType) event.getObject();
        long newValueForNoWorkingTime = calculationNoWorkingTime();
        if (!editionEventType.getNotWorkingTime().equals(newValueForNoWorkingTime)) {
            editionEventType.setNotWorkingTime(newValueForNoWorkingTime);
        }
		em.merge(editionEventType);
	}

	public List<EventType> getAllEventTypes() {
		return etDAO.getAllEventTypes();
	}

	// Block with privates methods

	private Long calculationNoWorkingTime() {
		return convertToMillisFromSelectedHour() + convertToMillisFromSelectedMinute();
	}

	private Long convertToMillisFromSelectedHour() {
		return selectedNoWorkHour * MILLIS_IN_HOUR;
	}

	private Long convertToMillisFromSelectedMinute() {
		return selectedNoWorkMinute * MILLIS_IN_MINUTE;
	}

	// Simple Getters and Setters

	public List<Integer> getPossibleHours() {
		List<Integer> possibleHours = new ArrayList<>();
		for (int hour = 0; hour < 25; hour++) {
			possibleHours.add(hour);
		}
		return possibleHours;
	}

	public List<Integer> getPossibleMinutes() {
		List<Integer> possibleMinutes = new ArrayList<>();
		for (int minute = 0; minute < 61; minute++) {
			possibleMinutes.add(minute);
		}
		return possibleMinutes;
	}

	public long getSelectedNoWorkHour() {
		return selectedNoWorkHour;
	}

	public void setSelectedNoWorkHour(long selectedNoWorkHour) {
		this.selectedNoWorkHour = selectedNoWorkHour;
	}

	public long getSelectedNoWorkMinute() {
		return selectedNoWorkMinute;
	}

	public void setSelectedNoWorkMinute(long selectedNoWorkMinute) {
		this.selectedNoWorkMinute = selectedNoWorkMinute;
	}

}