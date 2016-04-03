package org.onbrains.entity.event;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

public class Main {

	public static void main(String[] args) {
		LocalDateTime localDateTime = LocalDateTime.now();
		System.out.println("localDateTime = " + localDateTime.getNano() + " " + localDateTime);
		Calendar calendar = Calendar.getInstance();
		System.out.println("calendar = " + calendar.getTimeInMillis() + " " + calendar.getTime());

		Duration d = Duration.between(localDateTime, localDateTime.plusHours(5));
		System.out.println(
				"d = " + d.getSeconds() + " " + localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
						+ " - calendar " + calendar.getTimeInMillis());
	}

}