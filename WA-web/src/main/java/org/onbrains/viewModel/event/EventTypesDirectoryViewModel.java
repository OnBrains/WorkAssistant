package org.onbrains.viewModel.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJBTransactionRolledbackException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
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

/**
 * @author Naumov Oleg on 30.08.2015 15:30.
 */
@Named(value = "eventTypesDirectory")
@ViewScoped
public class EventTypesDirectoryViewModel implements Serializable {

	@Inject
	private EntityManagerUtils em;
	@Inject
	private EventTypeDAOInterface etDAO;

	private List<EventType> allTypes = new ArrayList<>();

	private TreeNode categoryNode;
	private TreeNode selectedCategoryNode;
	private List<EventType> typesBySelectedCategory = new ArrayList<>();
	private EventType newEventType;

	@PostConstruct
	public void postConstruct() {
		initCategoryNode();
	}

	public void onRowEdit(RowEditEvent event) {
		EventType editionEventType = (EventType) event.getObject();
		em.merge(editionEventType);
	}

	public void onCategoryNodeSelect(NodeSelectEvent event) {
		selectedCategoryNode = event.getTreeNode();
		buildResultTypesBy((EventCategory) selectedCategoryNode.getData());
	}

	public void openCreationDialog() {
		EventCategory category = selectedCategoryNode == null ? EventCategory.INFLUENCE_ON_WORKED_TIME
				: (EventCategory) selectedCategoryNode.getData();
		newEventType = new EventType(category);
		RequestContext.getCurrentInstance().update("creation_event_type_form");
	}

	public void createType() {
		em.persist(newEventType);
		allTypes.add(newEventType);
		buildResultTypesBy((EventCategory) selectedCategoryNode.getData());
		cleanParams();
	}

	public void cleanParams() {
		newEventType = null;
	}

	public void removeType(EventType removingType) {
		try {
			em.remove(em.merge(removingType));
            typesBySelectedCategory.remove(removingType);
		} catch (EJBTransactionRolledbackException ex) {
			Notification.error(String.format("Невозможну удалить тип: %s", removingType.getTitle()),
					em.formationMessageFrom(ex));
		}
	}

	public List<EventType> getAllTypes() {
		if (allTypes.isEmpty()) {
			allTypes = etDAO.getEventTypes(true, false);
		}
		return allTypes;
	}

	public String getSelectedCategory() {
		return selectedCategoryNode != null ? selectedCategoryNode.getData().toString() : StringUtils.EMPTY;
	}

	// *****************************************************************************************************************
	// Block with privates methods
	// *****************************************************************************************************************

	private void initCategoryNode() {
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

	// *****************************************************************************************************************
	// Simple Getters and Setters
	// *****************************************************************************************************************

	public EventCategory[] getEventCategories() {
		return EventCategory.values();
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