package com.ssu.urlshortener.url.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.ssu.urlshortener.global.dto.PageResponse;
import com.ssu.urlshortener.global.exception.url.InvalidUrlUpdateException;
import com.ssu.urlshortener.global.exception.url.ShortCodeGenerationException;
import com.ssu.urlshortener.global.exception.url.UrlExpiredException;
import com.ssu.urlshortener.global.exception.url.UrlNotFoundException;
import com.ssu.urlshortener.url.dto.CreateUrlRequest;
import com.ssu.urlshortener.url.dto.UpdateUrlRequest;
import com.ssu.urlshortener.url.dto.UrlResponse;
import com.ssu.urlshortener.url.entity.Url;
import com.ssu.urlshortener.url.generator.ShortCodeGenerator;
import com.ssu.urlshortener.url.repository.UrlRepository;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

	private static final String BASE_URL = "http://localhost:8080";
	private static final String SHORT_CODE = "AbCd12";
	private static final String ORIGINAL_URL = "https://example.com";

	@Mock
	private UrlRepository urlRepository;

	@Mock
	private ShortCodeGenerator shortCodeGenerator;

	@InjectMocks
	private UrlService urlService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(urlService, "baseUrl", BASE_URL);
	}

	@Nested
	@DisplayName("URL 생성")
	class Create {

		@Test
		@DisplayName("단축 URL을 생성한다")
		void create_success() {
			// given
			LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
			CreateUrlRequest request =
					new CreateUrlRequest(ORIGINAL_URL, expiresAt);

			given(shortCodeGenerator.generate()).willReturn(SHORT_CODE);
			given(urlRepository.existsByShortCode(SHORT_CODE))
					.willReturn(false);
			given(urlRepository.save(org.mockito.ArgumentMatchers.any(Url.class)))
					.willAnswer(invocation -> invocation.getArgument(0));

			// when
			UrlResponse response = urlService.create(request);

			// then
			assertThat(response.originalUrl()).isEqualTo(ORIGINAL_URL);
			assertThat(response.shortCode()).isEqualTo(SHORT_CODE);
			assertThat(response.shortUrl())
					.isEqualTo(BASE_URL + "/" + SHORT_CODE);
			assertThat(response.clickCount()).isZero();
			assertThat(response.expiresAt()).isEqualTo(expiresAt);

			ArgumentCaptor<Url> urlCaptor =
					ArgumentCaptor.forClass(Url.class);

			verify(urlRepository).save(urlCaptor.capture());

			Url savedUrl = urlCaptor.getValue();

			assertThat(savedUrl.getOriginalUrl()).isEqualTo(ORIGINAL_URL);
			assertThat(savedUrl.getShortCode()).isEqualTo(SHORT_CODE);
			assertThat(savedUrl.getExpiresAt()).isEqualTo(expiresAt);
		}

		@Test
		@DisplayName("생성된 단축 코드가 중복이면 다시 생성한다")
		void create_retryWhenShortCodeIsDuplicated() {
			// given
			String duplicatedCode = "Dupl12";
			String uniqueCode = "Unique1";

			CreateUrlRequest request =
					new CreateUrlRequest(ORIGINAL_URL, null);

			given(shortCodeGenerator.generate())
					.willReturn(duplicatedCode, uniqueCode);

			given(urlRepository.existsByShortCode(duplicatedCode))
					.willReturn(true);

			given(urlRepository.existsByShortCode(uniqueCode))
					.willReturn(false);

			given(urlRepository.save(org.mockito.ArgumentMatchers.any(Url.class)))
					.willAnswer(invocation -> invocation.getArgument(0));

			// when
			UrlResponse response = urlService.create(request);

			// then
			assertThat(response.shortCode()).isEqualTo(uniqueCode);

			verify(shortCodeGenerator, times(2)).generate();
			verify(urlRepository).existsByShortCode(duplicatedCode);
			verify(urlRepository).existsByShortCode(uniqueCode);
		}

		@Test
		@DisplayName("단축 코드 생성에 10회 실패하면 예외가 발생한다")
		void create_failAfterMaximumAttempts() {
			// given
			CreateUrlRequest request =
					new CreateUrlRequest(ORIGINAL_URL, null);

			given(shortCodeGenerator.generate()).willReturn(SHORT_CODE);
			given(urlRepository.existsByShortCode(SHORT_CODE))
					.willReturn(true);

			// when & then
			assertThatThrownBy(() -> urlService.create(request))
					.isInstanceOf(ShortCodeGenerationException.class);

			verify(shortCodeGenerator, times(10)).generate();
			verify(urlRepository, times(10))
					.existsByShortCode(SHORT_CODE);
			verify(urlRepository, never())
					.save(org.mockito.ArgumentMatchers.any(Url.class));
		}
	}

	@Nested
	@DisplayName("URL 리다이렉트")
	class Redirect {

		@Test
		@DisplayName("원본 URL을 반환하고 클릭 수를 증가시킨다")
		void redirect_success() {
			// given
			Url url = createUrl(ORIGINAL_URL, SHORT_CODE, null);

			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.of(url));

			// when
			String result = urlService.redirect(SHORT_CODE);

			// then
			assertThat(result).isEqualTo(ORIGINAL_URL);
			assertThat(url.getClickCount()).isEqualTo(1L);
		}

		@Test
		@DisplayName("단축 코드가 존재하지 않으면 예외가 발생한다")
		void redirect_notFound() {
			// given
			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> urlService.redirect(SHORT_CODE))
					.isInstanceOf(UrlNotFoundException.class);
		}

		@Test
		@DisplayName("만료된 URL이면 예외가 발생하고 클릭 수가 증가하지 않는다")
		void redirect_expired() {
			// given
			Url url = createUrl(
					ORIGINAL_URL,
					SHORT_CODE,
					LocalDateTime.now().minusDays(1)
			);

			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.of(url));

			// when & then
			assertThatThrownBy(() -> urlService.redirect(SHORT_CODE))
					.isInstanceOf(UrlExpiredException.class);

			assertThat(url.getClickCount()).isZero();
		}
	}

	@Nested
	@DisplayName("URL 목록 조회 및 검색")
	class GetUrls {

		@Test
		@DisplayName("검색어가 없으면 전체 URL 목록을 조회한다")
		void getUrls_withoutKeyword() {
			// given
			Pageable pageable = PageRequest.of(0, 10);

			Url firstUrl =
					createUrl("https://first.com", "First1", null);
			Url secondUrl =
					createUrl("https://second.com", "Second", null);

			Page<Url> urlPage = new PageImpl<>(
					List.of(firstUrl, secondUrl),
					pageable,
					2
			);

			given(urlRepository.findAll(pageable))
					.willReturn(urlPage);

			// when
			PageResponse<UrlResponse> response =
					urlService.getUrls(null, pageable);

			// then
			assertThat(response.content()).hasSize(2);
			assertThat(response.page()).isZero();
			assertThat(response.size()).isEqualTo(10);
			assertThat(response.totalElements()).isEqualTo(2);
			assertThat(response.totalPages()).isEqualTo(1);
			assertThat(response.first()).isTrue();
			assertThat(response.last()).isTrue();

			assertThat(response.content().get(0).shortCode())
					.isEqualTo("First1");

			verify(urlRepository).findAll(pageable);
			verify(
					urlRepository,
					never()
			).findByOriginalUrlContainingIgnoreCaseOrShortCodeContainingIgnoreCase(
					org.mockito.ArgumentMatchers.anyString(),
					org.mockito.ArgumentMatchers.anyString(),
					org.mockito.ArgumentMatchers.any(Pageable.class)
			);
		}

		@Test
		@DisplayName("검색어가 공백이면 전체 URL 목록을 조회한다")
		void getUrls_blankKeyword() {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			Page<Url> emptyPage = Page.empty(pageable);

			given(urlRepository.findAll(pageable))
					.willReturn(emptyPage);

			// when
			PageResponse<UrlResponse> response =
					urlService.getUrls("   ", pageable);

			// then
			assertThat(response.content()).isEmpty();

			verify(urlRepository).findAll(pageable);
		}

		@Test
		@DisplayName("검색어가 있으면 원본 URL 또는 단축 코드로 검색한다")
		void getUrls_withKeyword() {
			// given
			String keyword = "google";
			Pageable pageable = PageRequest.of(0, 10);

			Url url = createUrl(
					"https://google.com",
					"Googl1",
					null
			);

			Page<Url> urlPage =
					new PageImpl<>(List.of(url), pageable, 1);

			given(
					urlRepository
							.findByOriginalUrlContainingIgnoreCaseOrShortCodeContainingIgnoreCase(
									keyword,
									keyword,
									pageable
							)
			).willReturn(urlPage);

			// when
			PageResponse<UrlResponse> response =
					urlService.getUrls(keyword, pageable);

			// then
			assertThat(response.content()).hasSize(1);
			assertThat(response.content().get(0).originalUrl())
					.isEqualTo("https://google.com");

			verify(
					urlRepository
			).findByOriginalUrlContainingIgnoreCaseOrShortCodeContainingIgnoreCase(
					keyword,
					keyword,
					pageable
			);

			verify(urlRepository, never()).findAll(pageable);
		}

		@Test
		@DisplayName("검색어 앞뒤의 공백을 제거한다")
		void getUrls_trimKeyword() {
			// given
			String normalizedKeyword = "google";
			Pageable pageable = PageRequest.of(0, 10);
			Page<Url> emptyPage = Page.empty(pageable);

			given(
					urlRepository
							.findByOriginalUrlContainingIgnoreCaseOrShortCodeContainingIgnoreCase(
									normalizedKeyword,
									normalizedKeyword,
									pageable
							)
			).willReturn(emptyPage);

			// when
			urlService.getUrls("  google  ", pageable);

			// then
			verify(
					urlRepository
			).findByOriginalUrlContainingIgnoreCaseOrShortCodeContainingIgnoreCase(
					normalizedKeyword,
					normalizedKeyword,
					pageable
			);
		}
	}

	@Nested
	@DisplayName("URL 단건 조회")
	class GetUrl {

		@Test
		@DisplayName("단축 코드로 URL을 조회한다")
		void getUrl_success() {
			// given
			Url url = createUrl(ORIGINAL_URL, SHORT_CODE, null);

			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.of(url));

			// when
			UrlResponse response = urlService.getUrl(SHORT_CODE);

			// then
			assertThat(response.originalUrl()).isEqualTo(ORIGINAL_URL);
			assertThat(response.shortCode()).isEqualTo(SHORT_CODE);
			assertThat(response.shortUrl())
					.isEqualTo(BASE_URL + "/" + SHORT_CODE);
		}

		@Test
		@DisplayName("단축 코드가 존재하지 않으면 예외가 발생한다")
		void getUrl_notFound() {
			// given
			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> urlService.getUrl(SHORT_CODE))
					.isInstanceOf(UrlNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("URL 수정")
	class Update {

		@Test
		@DisplayName("원본 URL과 만료일을 수정한다")
		void update_success() {
			// given
			Url url = createUrl(ORIGINAL_URL, SHORT_CODE, null);

			String changedOriginalUrl = "https://changed.com";
			LocalDateTime changedExpiresAt =
					LocalDateTime.now().plusDays(30);

			UpdateUrlRequest request = new UpdateUrlRequest(
					changedOriginalUrl,
					changedExpiresAt
			);

			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.of(url));

			// when
			UrlResponse response =
					urlService.update(SHORT_CODE, request);

			// then
			assertThat(url.getOriginalUrl())
					.isEqualTo(changedOriginalUrl);
			assertThat(url.getExpiresAt())
					.isEqualTo(changedExpiresAt);

			assertThat(response.originalUrl())
					.isEqualTo(changedOriginalUrl);
			assertThat(response.expiresAt())
					.isEqualTo(changedExpiresAt);
		}

		@Test
		@DisplayName("원본 URL 앞뒤의 공백을 제거해서 수정한다")
		void update_trimOriginalUrl() {
			// given
			Url url = createUrl(ORIGINAL_URL, SHORT_CODE, null);

			UpdateUrlRequest request = new UpdateUrlRequest(
					"  https://changed.com  ",
					null
			);

			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.of(url));

			// when
			urlService.update(SHORT_CODE, request);

			// then
			assertThat(url.getOriginalUrl())
					.isEqualTo("https://changed.com");
		}

		@Test
		@DisplayName("수정할 값이 없으면 예외가 발생한다")
		void update_emptyRequest() {
			// given
			UpdateUrlRequest request =
					new UpdateUrlRequest(null, null);

			// when & then
			assertThatThrownBy(
					() -> urlService.update(SHORT_CODE, request)
			).isInstanceOf(InvalidUrlUpdateException.class);

			verify(urlRepository, never())
					.findByShortCode(SHORT_CODE);
		}

		@Test
		@DisplayName("단축 코드가 존재하지 않으면 예외가 발생한다")
		void update_notFound() {
			// given
			UpdateUrlRequest request =
					new UpdateUrlRequest("https://changed.com", null);

			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(
					() -> urlService.update(SHORT_CODE, request)
			).isInstanceOf(UrlNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("URL 삭제")
	class Delete {

		@Test
		@DisplayName("단축 코드에 해당하는 URL을 삭제한다")
		void delete_success() {
			// given
			Url url = createUrl(ORIGINAL_URL, SHORT_CODE, null);

			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.of(url));

			// when
			urlService.delete(SHORT_CODE);

			// then
			verify(urlRepository).delete(url);
		}

		@Test
		@DisplayName("단축 코드가 존재하지 않으면 예외가 발생한다")
		void delete_notFound() {
			// given
			given(urlRepository.findByShortCode(SHORT_CODE))
					.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> urlService.delete(SHORT_CODE))
					.isInstanceOf(UrlNotFoundException.class);

			verify(urlRepository, never())
					.delete(org.mockito.ArgumentMatchers.any(Url.class));
		}
	}

	private Url createUrl(
			String originalUrl,
			String shortCode,
			LocalDateTime expiresAt
	) {
		return Url.builder()
				.originalUrl(originalUrl)
				.shortCode(shortCode)
				.expiresAt(expiresAt)
				.build();
	}
}