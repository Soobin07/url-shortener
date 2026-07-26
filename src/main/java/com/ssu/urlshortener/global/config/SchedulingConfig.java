package com.ssu.urlshortener.global.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {

	@Bean
	public Clock applicationClock(
			@Value("${app.scheduler.zone:Asia/Tokyo}")
			String zone
	) {
		return Clock.system(ZoneId.of(zone));
	}
}