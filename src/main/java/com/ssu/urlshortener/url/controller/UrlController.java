package com.ssu.urlshortener.url.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssu.urlshortener.global.dto.PageResponse;
import com.ssu.urlshortener.url.dto.CreateUrlRequest;
import com.ssu.urlshortener.url.dto.UpdateUrlRequest;
import com.ssu.urlshortener.url.dto.UrlResponse;
import com.ssu.urlshortener.url.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "URL", description = "단축 URL 생성 및 관리 API")
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

	private final UrlService urlService;

	@Operation(summary = "단축 URL 생성", description = "원본 URL과 선택적 만료일을 입력하여 새로운 단축 URL을 생성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "단축 URL 생성 성공", content = @Content(schema = @Schema(implementation = UrlResponse.class))),
			@ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
			@ApiResponse(responseCode = "500", description = "단축 코드 생성 실패") })
	@PostMapping
	public ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
		UrlResponse response = urlService.create(request);

		return ResponseEntity.created(URI.create("/api/urls/" + response.shortCode())).body(response);
	}

	@Operation(summary = "URL 목록 조회 및 검색", description = """
			저장된 URL을 페이징하여 조회합니다.

			keyword가 있으면 원본 URL 또는 단축 코드에서 검색합니다.
			keyword가 없으면 전체 URL 목록을 조회합니다.
			""")
	@ApiResponse(responseCode = "200", description = "목록 조회 성공")
	@GetMapping
	public ResponseEntity<PageResponse<UrlResponse>> getUrls(
			@Parameter(description = "원본 URL 또는 단축 코드 검색어", example = "google") @RequestParam(name = "keyword", required = false) String keyword,
			@Parameter(description = """
					페이징 정보입니다.

					기본값:
					- page: 0
					- size: 10
					- sort: createdAt,desc
					""") @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		PageResponse<UrlResponse> response = urlService.getUrls(keyword, pageable);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "URL 단건 조회", description = "단축 코드를 사용하여 URL 상세 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = UrlResponse.class))),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 단축 코드") })
	@GetMapping("/{shortCode}")
	public ResponseEntity<UrlResponse> getUrl(
			@Parameter(description = "조회할 단축 코드", example = "AbCd12") @PathVariable(name = "shortCode") String shortCode) {

		return ResponseEntity.ok(urlService.getUrl(shortCode));
	}

	@Operation(summary = "URL 삭제", description = "단축 코드에 해당하는 URL을 삭제합니다.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "삭제 성공"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 단축 코드") })
	@DeleteMapping("/{shortCode}")
	public ResponseEntity<Void> delete(
			@Parameter(description = "삭제할 단축 코드", example = "AbCd12") @PathVariable(name = "shortCode") String shortCode) {

		urlService.delete(shortCode);

		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "URL 수정", description = """
			단축 코드에 해당하는 URL 정보를 부분 수정합니다.

			수정 가능한 정보:
			- 원본 URL
			- 만료일
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = UrlResponse.class))),
			@ApiResponse(responseCode = "400", description = "유효하지 않은 수정 요청"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 단축 코드") })
	@PatchMapping("/{shortCode}")
	public ResponseEntity<UrlResponse> update(
			@Parameter(description = "수정할 단축 코드", example = "AbCd12") @PathVariable(name = "shortCode") String shortCode,
			@Valid @RequestBody UpdateUrlRequest request) {
		UrlResponse response = urlService.update(shortCode, request);

		return ResponseEntity.ok(response);
	}
}