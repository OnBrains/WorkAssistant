package org.onbrains.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import java.io.Serializable;

/**
 * @author Naumov Oleg on 27.07.2015 20:24. <br/>
 *         Родительский класс для всех сущностей.
 */
@MappedSuperclass
public class SuperClass implements Serializable {

	@Id
	@GeneratedValue(generator = "genId")
	@SequenceGenerator(name = "genId", sequenceName = "GEN_WA_ID", allocationSize = 1)
	private Long id;

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return getClass().getName() + "-" + this.getId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		SuperClass superClass = (SuperClass) o;

		if (id != null ? !id.equals(superClass.id) : superClass.id != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
