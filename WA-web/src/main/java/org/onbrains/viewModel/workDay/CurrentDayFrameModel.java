package org.onbrains.viewModel.workDay;

import org.onbrains.dao.workDay.WorkDayDAOInterface;
import org.onbrains.entity.workDay.WorkDay;
import org.onbrains.service.SessionUtil;
import org.onbrains.utils.parsing.DateFormatService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Naumov Oleg on 09.10.2015 21:16.
 */
@Named
@ConversationScoped
@Transactional
public class CurrentDayFrameModel implements Serializable {

	@Inject
	private WorkDayDAOInterface wdDAO;

	private WorkDay currentWorkDay;

	@PostConstruct
	public void postConstruct() {
		initCurrentWorkDay();
	}

	/**
	 * Формирует заголовок для блока управления текущим рабочим днем. Заголовок формируется из даты в формате 'dd EE.' +
	 * состояние рабочего дня.
	 *
	 * @return Заголовок для блока управления текущим рабочим днем.
	 */
	public String getLegendValue() {
		return currentWorkDay != null ? DateFormatService.toDDEE(currentWorkDay.getDay().getDay()) + " - "
				+ currentWorkDay.getState().getDesc() : "Не найдено";
	}

	private void initCurrentWorkDay() {
		currentWorkDay = wdDAO.getCurrentDayInfo(new Date(), SessionUtil.getWorker());
	}

    public WorkDay getCurrentWorkDay() {
        return currentWorkDay;
    }

}