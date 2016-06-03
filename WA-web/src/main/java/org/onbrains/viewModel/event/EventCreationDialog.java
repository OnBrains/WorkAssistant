package org.onbrains.viewModel.event;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.onbrains.entity.workDay.WorkDayState.WORKED;
import static org.onbrains.utils.parsing.DateFormatService.dateToLocalDate;
import static org.onbrains.utils.parsing.DateFormatService.fixDate;
import static org.onbrains.utils.parsing.DateFormatService.localDateToDate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.event.EventType;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.information.Notification;
import org.primefaces.context.RequestContext;

/**
 * @author Naumov Oleg on 29.05.2016.
 */
@Named
@ViewScoped
public class EventCreationDialog implements Serializable {

	private static final long serialVersionUID = -4829772647839704399L;

	@Inject
	private EntityManagerUtils em;
	@Inject
	private EventTypeDAOInterface etDAO;
	@Inject
	private WorkDayDAOInterface wdDAO;

	private EventType eventType;

	private boolean createInterval;
	private WorkDay workDay;
	private Date startDay;
	private Date endDay;

	private boolean fullDay;
	private Date startTime;
	private Date endTime;

	// *****************************************************************************************************************
	// Service methods
	// *****************************************************************************************************************

	public void create() {
		if (!createInterval) {
			startDay = workDay != null ? localDateToDate(workDay.getDay().getDate()) : startDay;
			createEvent(startDay);
		} else {
			GregorianCalendar startDayGregorianCalendar = new GregorianCalendar();
			startDayGregorianCalendar.setTime(startDay);

			while (startDayGregorianCalendar.getTime().before(endDay)
					|| startDayGregorianCalendar.getTime().equals(endDay)) {
				createEvent(startDayGregorianCalendar.getTime());
				startDayGregorianCalendar.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		cleanCreationParams();
	}

	public void cleanCreationParams() {
		eventType = null;
		createInterval = false;
		startDay = null;
		endDay = null;
		fullDay = false;
		startTime = null;
		endTime = null;
	}

	public List<EventType> getEventTypes() {
		return etDAO.getEventTypes(true);
	}

	// *****************************************************************************************************************
	// Private methods
	// *****************************************************************************************************************

	private void createEvent(Date date) {
		LocalDate eventDay = dateToLocalDate(date);
		LocalDateTime startTimeValue = startTime != null
				? LocalDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault()) : null;
		LocalDateTime endTimeValue = endTime != null
				? LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault()) : null;
		if (createInterval) {
			workDay = wdDAO.getWorkDay(eventDay, SessionUtil.getWorker());
		}
		Event newEvent = new Event(workDay, eventType, eventType.getTitle(), fullDay, fixDate(startTimeValue, eventDay),
				fixDate(endTimeValue, eventDay));
		if (fullDay) {
			workDay.setState(WORKED);
		}
		String result = workDay.addEvent(newEvent);
		if (result.equals(EMPTY)) {
			em.persist(newEvent);
			em.merge(workDay);
			RequestContext.getCurrentInstance().execute("PF('eventCreationDialogVar').hide()");
			return;
		}
		Notification.warn("Невозможно создать событие", result);
	}

	// *****************************************************************************************************************
	// Simple Getters and Setters
	// *****************************************************************************************************************

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public boolean isCreateInterval() {
		return createInterval;
	}

	public void setCreateInterval(boolean createInterval) {
		this.createInterval = createInterval;
	}

	public void setWorkDay(WorkDay workDay) {
		this.workDay = workDay;
	}

	public Date getStartDay() {
		return workDay == null ? startDay : localDateToDate(workDay.getDay().getDate());
	}

	public void setStartDay(Date startDay) {
		this.startDay = startDay;
	}

	public Date getEndDay() {
		return endDay;
	}

	public void setEndDay(Date endDay) {
		this.endDay = endDay;
	}

	public boolean isFullDay() {
		return fullDay;
	}

	public void setFullDay(boolean fullDay) {
		this.fullDay = fullDay;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}