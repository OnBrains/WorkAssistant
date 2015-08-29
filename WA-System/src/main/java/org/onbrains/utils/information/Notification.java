package org.onbrains.utils.information;

import org.primefaces.context.RequestContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * @author Naumov Oleg on 26.04.2015 20:58.
 */

public class Notification {

	public static void info(String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage("msg_for_info",
				new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail == null ? "" : detail));
		if (RequestContext.getCurrentInstance().isAjaxRequest()) {
			RequestContext.getCurrentInstance().update("msg_info");
		}
	}

	public static void warn(String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage("msg_for_warn",
				new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail == null ? "" : detail));
		if (!RequestContext.getCurrentInstance().isAjaxRequest()) {
			return;
		}
		RequestContext.getCurrentInstance().update("msg_warn");
	}

	public static void error(String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail == null ? "" : detail));
	}

}