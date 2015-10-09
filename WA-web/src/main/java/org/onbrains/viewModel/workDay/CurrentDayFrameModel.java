package org.onbrains.viewModel.workDay;

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
	private WorkDayFrameModel workDayModel;

	@PostConstruct
	public void postConstruct() {
		workDayModel.initWorkDay(new Date());
	}

	/**
	 * Формирует заголовок для блока управления текущим рабочим днем. Заголовок формируется из даты в формате 'dd EE.' +
	 * состояние рабочего дня.
	 *
	 * @return Заголовок для блока управления текущим рабочим днем.
	 */
	public String getLegendValue() {
		return workDayModel.getWorkDay() != null ? DateFormatService.toDDEE(workDayModel.getWorkDay().getDay().getDay())
				+ " - " + workDayModel.getWorkDay().getState().getDesc() : "Не найдено";
	}

}