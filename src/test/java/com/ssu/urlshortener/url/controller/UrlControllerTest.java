package com.ssu.urlshortener.url.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ssu.urlshortener.global.dto.PageResponse;
import com.ssu.urlshortener.global.exception.GlobalExceptionHandler;
import com.ssu.urlshortener.global.exception.url.InvalidUrlUpdateException;
import com.ssu.urlshortener.global.exception.url.UrlNotFoundException;
import com.ssu.urlshortener.url.dto.UpdateUrlRequest;
import com.ssu.urlshortener.url.dto.UrlResponse;
import com.ssu.urlshortener.url.service.UrlService;

@WebMvcTest(UrlController.class)
@Import(GlobalExceptionHandler.class)
class UrlControllerTest {

	private static final String BASE_URL = "http://localhost:8080";
	private static final String SHORT_CODE = "AbCd12";
	private static final String ORIGINAL_URL = "https://example.com";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UrlService urlService;

	@Nested
	@DisplayName("URL 생성 API")
	class Create {

		@Test
		@DisplayName("단축 URL을 생성하면 201 응답을 반환한다")
		void create_success() throws Exception {
			// given
			LocalDateTime expiresAt =
					LocalDateTime.of(2027, 12, 31, 23, 59, 59);

			UrlResponse response = createResponse(
					1L,
					ORIGINAL_URL,
					SHORT_CODE,
					0L,
					expiresAt
			);

			given(urlService.create(any()))
					.willReturn(response);

			String requestBody = """
					{
					  "originalUrl": "https://example.com",
					  "expiresAt": "2027-12-31T23:59:59"
					}
					""";

			// when & then
			mockMvc.perform(
						post("/api/urls")
								.contentType(MediaType.APPLICATION_JSON)
								.content(requestBody)
					)
					.andExpect(status().isCreated())
					.andExpect(content().contentTypeCompatibleWith(
							MediaType.APPLICATION_JSON
					))
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.originalUrl")
							.value(ORIGINAL_URL))
					.andExpect(jsonPath("$.shortCode")
							.value(SHORT_CODE))
					.andExpect(jsonPath("$.shortUrl")
							.value(BASE_URL + "/" + SHORT_CODE))
					.andExpect(jsonPath("$.clickCount").value(0))
					.andExpect(jsonPath("$.expiresAt")
							.value("2027-12-31T23:59:59"));

			verify(urlService).create(any());
		}

		@Test
		@DisplayName("원본 URL이 비어 있으면 400 응답을 반환한다")
		void create_blankOriginalUrl() throws Exception {
			// given
			String requestBody = """
					{
					  "originalUrl": ""
					}
					""";

			// when & then
			mockMvc.perform(
						post("/api/urls")
								.contentType(MediaType.APPLICATION_JSON)
								.content(requestBody)
					)
					.andExpect(status().isBadRequest());

			verify(urlService, never()).create(any());
		}

		@Test
		@DisplayName("만료일이 과거이면 400 응답을 반환한다")
		void create_pastExpiresAt() throws Exception {
			// given
			String requestBody = """
					{
					  "originalUrl": "https://example.com",
					  "expiresAt": "2020-01-01T00:00:00"
					}
					""";

			// when & then
			mockMvc.perform(
						post("/api/urls")
								.contentType(MediaType.APPLICATION_JSON)
								.content(requestBody)
					)
					.andExpect(status().isBadRequest());

			verify(urlService, never()).create(any());
		}
	}

	@Nested
	@DisplayName("URL 목록 조회 및 검색 API")
	class GetUrls {

		@Test
		@DisplayName("URL 목록을 페이징 조회한다")
		void getUrls_success() throws Exception {
			// given
			UrlResponse first = createResponse(
					1L,
					"https://first.com",
					"First1",
					3L,
					null
			);

			UrlResponse second = createResponse(
					2L,
					"https://second.com",
					"Second",
					7L,
					null
			);

			PageResponse<UrlResponse> response =
					new PageResponse<>(
							List.of(first, second),
							0,
							10,
							2L,
							1,
							true,
							true
					);

			given(urlService.getUrls(
					eq(null),
					any(Pageable.class)
			)).willReturn(response);

			// when & then
			mockMvc.perform(
						get("/api/urls")
								.param("page", "0")
								.param("size", "10")
					)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content", hasSize(2)))
					.andExpect(jsonPath("$.content[0].shortCode")
							.value("First1"))
					.andExpect(jsonPath("$.content[1].shortCode")
							.value("Second"))
					.andExpect(jsonPath("$.page").value(0))
					.andExpect(jsonPath("$.size").value(10))
					.andExpect(jsonPath("$.totalElements").value(2))
					.andExpect(jsonPath("$.totalPages").value(1))
					.andExpect(jsonPath("$.first").value(true))
					.andExpect(jsonPath("$.last").value(true));

			verify(urlService).getUrls(
					eq(null),
					any(Pageable.class)
			);
		}

		@Test
		@DisplayName("검색어로 URL 목록을 검색한다")
		void getUrls_withKeyword() throws Exception {
			// given
			String keyword = "google";

			UrlResponse url = createResponse(
					1L,
					"https://google.com",
					"Google1",
					5L,
					null
			);

			PageResponse<UrlResponse> response =
					new PageResponse<>(
							List.of(url),
							0,
							10,
							1L,
							1,
							true,
							true
					);

			given(urlService.getUrls(
					eq(keyword),
					any(Pageable.class)
			)).willReturn(response);

			// when & then
			mockMvc.perform(
						get("/api/urls")
								.param("keyword", keyword)
								.param("page", "0")
								.param("size", "10")
					)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content", hasSize(1)))
					.andExpect(jsonPath("$.content[0].originalUrl")
							.value("https://google.com"))
					.andExpect(jsonPath("$.content[0].shortCode")
							.value("Google1"))
					.andExpect(jsonPath("$.totalElements").value(1));

			verify(urlService).getUrls(
					eq(keyword),
					any(Pageable.class)
			);
		}
	}

	@Nested
	@DisplayName("URL 단건 조회 API")
	class GetUrl {

		@Test
		@DisplayName("단축 코드로 URL을 조회한다")
		void getUrl_success() throws Exception {
			// given
			UrlResponse response = createResponse(
					1L,
					ORIGINAL_URL,
					SHORT_CODE,
					5L,
					null
			);

			given(urlService.getUrl(SHORT_CODE))
					.willReturn(response);

			// when & then
			mockMvc.perform(get("/api/urls/{shortCode}", SHORT_CODE))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.originalUrl")
							.value(ORIGINAL_URL))
					.andExpect(jsonPath("$.shortCode")
							.value(SHORT_CODE))
					.andExpect(jsonPath("$.shortUrl")
							.value(BASE_URL + "/" + SHORT_CODE))
					.andExpect(jsonPath("$.clickCount").value(5));

			verify(urlService).getUrl(SHORT_CODE);
		}

		@Test
		@DisplayName("단축 코드가 존재하지 않으면 404 응답을 반환한다")
		void getUrl_notFound() throws Exception {
			// given
			given(urlService.getUrl(SHORT_CODE))
					.willThrow(new UrlNotFoundException());

			// when & then
			mockMvc.perform(get("/api/urls/{shortCode}", SHORT_CODE))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("URL_001"))
					.andExpect(jsonPath("$.message")
							.value("존재하지 않는 단축 URL입니다."));

			verify(urlService).getUrl(SHORT_CODE);
		}
	}

	@Nested
	@DisplayName("URL 수정 API")
	class Update {

		@Test
		@DisplayName("원본 URL과 만료일을 수정한다")
		void update_success() throws Exception {
			// given
			String changedUrl = "https://changed.com";
			LocalDateTime expiresAt =
					LocalDateTime.of(2027, 12, 31, 23, 59, 59);

			UrlResponse response = createResponse(
					1L,
					changedUrl,
					SHORT_CODE,
					5L,
					expiresAt
			);

			given(urlService.update(
					eq(SHORT_CODE),
					any(UpdateUrlRequest.class)
			)).willReturn(response);

			String requestBody = """
					{
					  "originalUrl": "https://changed.com",
					  "expiresAt": "2027-12-31T23:59:59"
					}
					""";

			// when & then
			mockMvc.perform(
						patch("/api/urls/{shortCode}", SHORT_CODE)
								.contentType(MediaType.APPLICATION_JSON)
								.content(requestBody)
					)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.originalUrl")
							.value(changedUrl))
					.andExpect(jsonPath("$.shortCode")
							.value(SHORT_CODE))
					.andExpect(jsonPath("$.expiresAt")
							.value("2027-12-31T23:59:59"));

			verify(urlService).update(
					eq(SHORT_CODE),
					any(UpdateUrlRequest.class)
			);
		}

		@Test
		@DisplayName("수정할 값이 없으면 400 응답을 반환한다")
		void update_emptyRequest() throws Exception {
			// given
			given(urlService.update(
					eq(SHORT_CODE),
					any(UpdateUrlRequest.class)
			)).willThrow(new InvalidUrlUpdateException());

			// when & then
			mockMvc.perform(
						patch("/api/urls/{shortCode}", SHORT_CODE)
								.contentType(MediaType.APPLICATION_JSON)
								.content("{}")
					)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.code").value("URL_004"))
					.andExpect(jsonPath("$.message")
							.value("수정할 URL 정보가 없습니다."));

			verify(urlService).update(
					eq(SHORT_CODE),
					any(UpdateUrlRequest.class)
			);
		}

		@Test
		@DisplayName("만료일이 과거이면 400 응답을 반환한다")
		void update_pastExpiresAt() throws Exception {
			// given
			String requestBody = """
					{
					  "expiresAt": "2020-01-01T00:00:00"
					}
					""";

			// when & then
			mockMvc.perform(
						patch("/api/urls/{shortCode}", SHORT_CODE)
								.contentType(MediaType.APPLICATION_JSON)
								.content(requestBody)
					)
					.andExpect(status().isBadRequest());

			verify(urlService, never()).update(
					eq(SHORT_CODE),
					any(UpdateUrlRequest.class)
			);
		}
	}

	@Nested
	@DisplayName("URL 삭제 API")
	class Delete {

		@Test
		@DisplayName("URL을 삭제하면 204 응답을 반환한다")
		void delete_success() throws Exception {
			// given
			willDoNothing()
					.given(urlService)
					.delete(SHORT_CODE);

			// when & then
			mockMvc.perform(
						delete("/api/urls/{shortCode}", SHORT_CODE)
					)
					.andExpect(status().isNoContent())
					.andExpect(content().string(""));

			verify(urlService).delete(SHORT_CODE);
		}

		@Test
		@DisplayName("단축 코드가 존재하지 않으면 404 응답을 반환한다")
		void delete_notFound() throws Exception {
			// given
			org.mockito.BDDMockito.willThrow(
					new UrlNotFoundException()
			).given(urlService).delete(SHORT_CODE);

			// when & then
			mockMvc.perform(
						delete("/api/urls/{shortCode}", SHORT_CODE)
					)
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("URL_001"))
					.andExpect(jsonPath("$.message")
							.value("존재하지 않는 단축 URL입니다."));

			verify(urlService).delete(SHORT_CODE);
		}
	}

	private UrlResponse createResponse(
			Long id,
			String originalUrl,
			String shortCode,
			long clickCount,
			LocalDateTime expiresAt
	) {
		return new UrlResponse(
				id,
				originalUrl,
				shortCode,
				BASE_URL + "/" + shortCode,
				clickCount,
				expiresAt,
				LocalDateTime.of(2026, 7, 23, 12, 0));
	}
}