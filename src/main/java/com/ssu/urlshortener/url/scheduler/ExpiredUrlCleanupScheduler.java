package com.ssu.urlshortener.url.scheduler;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ssu.urlshortener.url.service.UrlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredUrlCleanupScheduler {

	private final UrlService urlService;
	private final Clock applicationClock;

	@Scheduled(
			cron = "${app.scheduler.expired-url-cleanup-cron:0 0 3 * * *}",
			zone = "${app.scheduler.zone:Asia/Tokyo}"
	)
	public void cleanupExpiredUrls() {
		LocalDateTime now = LocalDateTime.now(applicationClock);

		int deletedCount = urlService.deleteExpiredUrls(now);

		log.info(
				"Expired URL cleanup completed. deletedCount={}, executedAt={}",
				deletedCount,
				now
		);
	}
}