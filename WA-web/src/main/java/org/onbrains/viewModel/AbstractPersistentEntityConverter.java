package org.onbrains.viewModel;

import org.onbrains.dao.EntityManagerUtils;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Naumov Oleg on 23.08.2015 19:05.
 */
@Named
@RequestScoped
public class AbstractPersistentEntityConverter implements Converter, Serializable {

	private static final Pattern SERIALIZED_FORMAT = Pattern.compile("(.*)\\-([0-9]+)");

    @Inject
	private EntityManagerUtils em;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || "".equals(value)) {
			return null;
		}

		Matcher matcher = SERIALIZED_FORMAT.matcher(value);
		if (!matcher.matches()) {
			throw new ConverterException(
					new FacesMessage("'" + value + "' неверное значение, необходимо выбрать значение из предложеных"));
		}

		Class<?> entityClass = null;
		Long primaryKey = null;
		try {
			entityClass = getClass(matcher.group(1));
			primaryKey = getId(matcher.group(2));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return em.find(entityClass, primaryKey);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return value.toString();
	}

	private Class<?> getClass(String classPath) throws ClassNotFoundException {
		return Class.forName(classPath);
	}

	private Long getId(String idValue) {
		return Long.parseLong(idValue);
	}
}
