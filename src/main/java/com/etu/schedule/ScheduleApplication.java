package com.etu.schedule;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@EnableScheduling
@SpringBootApplication
public class ScheduleApplication {

	public static final List<String> DAY_FROM_ETU = List.of("", "MON", "TUE", "WED", "THU", "FRI", "SAT");
	public static final List<String> DAY = List.of("", "Понедельник:", "Вторник:", "Среда:", "Четверг:", "Пятница:", "Суббота:");
	public static final List<String> TIME = List.of("08:00", "09:50", "11:40", "13:40", "15:30", "17:20", "19:05", "20:50");

	public static void main(String[] args) {
		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
		SpringApplication.run(ScheduleApplication.class, args);
	}

}
