package org.onbrains.viewModel.workDay;

import org.onbrains.entity.workDay.WorkDay;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 09.10.2015 21:42.
 */
@Named
@ConversationScoped
@Transactional
public class WorkDayCardViewModel implements Serializable {

    @PersistenceContext
    private EntityManager em;

    private WorkDay selectedWorkDay;

    @PostConstruct
    public void postConstruct() {
        selectedWorkDay = initSelectedWorkDay();
    }

    public WorkDay initSelectedWorkDay() {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("workDay");
        return path == null ? null : em.find(WorkDay.class, Long.parseLong(path.split("-")[1]));
    }

    public WorkDay getSelectedWorkDay() {
        return selectedWorkDay;
    }

}
