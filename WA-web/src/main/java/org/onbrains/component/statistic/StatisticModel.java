package org.onbrains.component.statistic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;

/**
 * Модель для панели статистики. Работает с коллекцией {@linkplain StatisticValue значений}.
 * <p/>
 * Created by Naumov Oleg on 03.01.2016.
 */
@FacesComponent(value = "statistic")
public class StatisticModel extends UINamingContainer {

	private static final String VALUE_STYLE = "background-color: %s; padding: 4px; width: %s; display: table-cell;";
	private static final String VALUES = "statisticValues";

	public List<StatisticValue> getValues() {
		List<StatisticValue> values = (List<StatisticValue>) getAttributes().get(VALUES);
        List<StatisticValue> allValues = new ArrayList<>(values);
        for (StatisticValue value : allValues) {
			if (value.getCount() == 0) {
				values.remove(value);
			}
		}
		calculatePercentageFor(values);
		return values;
	}

	public String getStyleFor(StatisticValue value) {
		return String.format(VALUE_STYLE, value.getColor(), value.getPercentageValue());
	}

	private void calculatePercentageFor(List<StatisticValue> values) {
		long sumValue = 0;
		for (StatisticValue value : values) {
			sumValue = sumValue + value.getCount();
		}
		if (sumValue != 0) {
			for (StatisticValue value : values) {
				value.setPercentage(getPercentage(sumValue, value.getCount()));
			}
		}
	}

	private float getPercentage(long sumValue, long value) {
		return (float) (value * 100 / sumValue);
	}

}