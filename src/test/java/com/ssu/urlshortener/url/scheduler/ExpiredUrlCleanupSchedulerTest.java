package com.ssu.urlshortener.url.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ssu.urlshortener.url.service.UrlService;

@ExtendWith(MockitoExtension.class)
class ExpiredUrlCleanupSchedulerTest {

	private static final ZoneId ZONE_ID =
			ZoneId.of("Asia/Tokyo");

	@Mock
	private UrlService urlService;

	private ExpiredUrlCleanupScheduler scheduler;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(
				Instant.parse("2026-07-26T18:00:00Z"),
				ZONE_ID
		);

		scheduler = new ExpiredUrlCleanupScheduler(
				urlService,
				fixedClock
		);
	}

	@Test
	@DisplayName("현재 시각을 기준으로 만료된 URL을 삭제한다")
	void cleanupExpiredUrls_success() {
		// given
		given(urlService.deleteExpiredUrls(
				LocalDateTime.of(2026, 7, 27, 3, 0)
		)).willReturn(3);

		// when
		scheduler.cleanupExpiredUrls();

		// then
		ArgumentCaptor<LocalDateTime> nowCaptor =
				ArgumentCaptor.forClass(LocalDateTime.class);

		verify(urlService).deleteExpiredUrls(nowCaptor.capture());

		assertThat(nowCaptor.getValue())
				.isEqualTo(LocalDateTime.of(2026, 7, 27, 3, 0));
	}
}