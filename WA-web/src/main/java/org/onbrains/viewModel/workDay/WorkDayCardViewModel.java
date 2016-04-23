package org.onbrains.viewModel.workDay;

import java.time.temporal.ChronoField;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.entity.event.Event;
import org.onbrains.entity.workDay.WorkDay;

/**
 * @author Naumov Oleg on 09.10.2015 21:42.
 */
@Named
@ViewScoped
public class WorkDayCardViewModel extends WorkDayFrameModel {

	private static final long serialVersionUID = 423770186596064662L;

	@Inject
	private EntityManagerUtils em;

	public void preRender() {
		setWorkDay(initWorkDay());
	}

	// @PostConstruct
	// public void postConstruct() {
	// workDay = initWorkDay();
	// }

	public int getMinHourForEndEvent(Event event) {
		return event.getStartTime().get(ChronoField.HOUR_OF_DAY);
	}

	// *****************************************************************************************************************
	// Private methods
	// *****************************************************************************************************************

	private WorkDay initWorkDay() {
		String path = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("workDay");
		return path == null ? null : em.find(WorkDay.class, Long.parseLong(path.split("-")[1]));
	}

	// *****************************************************************************************************************
	// Simple getters and setters
	// *****************************************************************************************************************

	public WorkDay getWorkDay() {
		return workDay;
	}

}