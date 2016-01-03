package org.onbrains.viewModel.event;

import org.onbrains.dao.EntityManagerUtils;
import org.onbrains.dao.workDayEvent.EventTypeDAOInterface;
import org.onbrains.entity.event.EventCategory;
import org.onbrains.entity.event.EventType;
import org.onbrains.utils.information.Notification;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Naumov Oleg on 30.08.2015 15:30.
 */

@Named(value = "eventTypesDirectory")
@SessionScoped
public class EventTypesDirectoryViewModel implements Serializable {

	private static final long MILLIS_IN_MINUTE = 60000;
	private static final long MILLIS_IN_HOUR = 3600000;

	@Inject
	private EntityManagerUtils em;
	@Inject
	private EventTypeDAOInterface etDAO;

	private long selectedNoWorkHour = 0;
	private long selectedNoWorkMinute = 0;
	private List<EventType> allTypes = new ArrayList<>();

	private TreeNode categoryNode;
	private TreeNode selectedCategoryNode;
	private List<EventType> typesBySelectedCategory = new ArrayList<>();
	private EventType newEventType;

	@PostConstruct
	public void postConstruct() {
		initializationCategoryNode();
	}

	public void onRowEdit(RowEditEvent event) {
		EventType editionEventType = (EventType) event.getObject();
		editionEventType.setNotWorkingTime(calculationNoWorkingTime());
		em.merge(editionEventType);
	}

	public void onCategoryNodeSelect(NodeSelectEvent event) {
		selectedCategoryNode = event.getTreeNode();
		buildResultTypesBy((EventCategory) selectedCategoryNode.getData());
	}

	public void onCreationTypeStart() {
		EventCategory category = selectedCategoryNode == null ? EventCategory.INFLUENCE_ON_WORKED_TIME
				: (EventCategory) selectedCategoryNode.getData();
		newEventType = new EventType(category);
		RequestContext.getCurrentInstance().update("creation_event_type_form");
	}

	public void createType() {
		newEventType.setNotWorkingTime(calculationNoWorkingTime());
		em.persist(newEventType);
		allTypes.add(newEventType);
		buildResultTypesBy((EventCategory) selectedCategoryNode.getData());
		cleanParams();
	}

	public void cancelCreationType() {
		cleanParams();
	}

	// FIXME: без flush нельзя пойтать Exeption
	public void removeType(EventType removingType) {
		try {
			em.remove(em.merge(removingType));
//			em.flush();
			typesBySelectedCategory.remove(removingType);
		} catch (PersistenceException constraintException) {
			Notification.warn(String.format("Невозможну удалить тип: %s", removingType.getTitle()),
					"Данный тип имеет зависимые записи");
		}
	}

	public List<EventType> getAllTypes() {
		if (allTypes.isEmpty()) {
			allTypes = etDAO.getEventTypes(true, false);
		}
		return allTypes;
	}

	// *****************************************************************************************************************
	// Block with privates methods
	// *****************************************************************************************************************

	private Long calculationNoWorkingTime() {
		return convertToMillisFromSelectedHour() + convertToMillisFromSelectedMinute();
	}

	private Long convertToMillisFromSelectedHour() {
		return selectedNoWorkHour * MILLIS_IN_HOUR;
	}

	private Long convertToMillisFromSelectedMinute() {
		return selectedNoWorkMinute * MILLIS_IN_MINUTE;
	}

	private void initializationCategoryNode() {
		categoryNode = new DefaultTreeNode("root", null);
		for (EventCategory category : EventCategory.values()) {
			new DefaultTreeNode(category, categoryNode);
		}
	}

	private void buildResultTypesBy(EventCategory selectedCategory) {
		typesBySelectedCategory.clear();
		for (EventType type : getAllTypes()) {
			if (type.getCategory().equals(selectedCategory)) {
				typesBySelectedCategory.add(type);
			}
		}
	}

	private void cleanParams() {
		newEventType = null;
		selectedNoWorkHour = 0;
		selectedNoWorkMinute = 0;
	}

	// *****************************************************************************************************************
	// Simple Getters and Setters
	// *****************************************************************************************************************

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

	public EventCategory[] getEventCategories() {
		return EventCategory.values();
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

	public TreeNode getCategoryNode() {
		return categoryNode;
	}

	public List<EventType> getTypesBySelectedCategory() {
		return typesBySelectedCategory;
	}

	public EventType getNewEventType() {
		return newEventType;
	}

	public void setNewEventType(EventType newEventType) {
		this.newEventType = newEventType;
	}

}