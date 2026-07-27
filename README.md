# URL Shortener

긴 URL을 짧은 URL로 변환하고 관리할 수 있는 URL Shortener REST API입니다.

Spring Boot와 JPA를 기반으로 구현했으며, CRUD API, 리다이렉트, URL 만료, 자동 정리 Scheduler, Validation, Swagger, 테스트 코드 등을 포함한 백엔드 프로젝트입니다.

---

# Tech Stack

## Backend

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring Validation

## Database

- PostgreSQL

## Documentation

- Swagger (springdoc-openapi)

## Test

- JUnit5
- Mockito
- MockMvc

---

# Architecture

```
Client
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

프로젝트는 Controller-Service-Repository 계층 구조를 기반으로 구현했습니다.

---

# Project Structure

```
src
├── global
│   ├── config
│   ├── dto
│   ├── entity
│   └── exception
│
└── url
    ├── controller
    ├── dto
    ├── entity
    ├── generator
    ├── repository
    ├── scheduler
    ├── service
    └── validation
```

---

# Features

## URL 생성

- 긴 URL을 단축 URL로 생성
- 만료일(Optional) 지정 가능
- 중복되지 않는 Short Code 생성

---

## URL 리다이렉트

- Short Code를 통해 원본 URL로 이동
- 클릭 수 자동 증가
- 만료 URL 접근 시 예외 처리

---

## URL 조회

- 전체 URL 조회
- Pagination 지원
- Keyword 검색 지원

---

## URL 수정

- 원본 URL 수정
- 만료일 수정

---

## URL 삭제

- 단축 URL 삭제

---

## Scheduler

매일 새벽 3시(JST)에 만료된 URL을 자동 삭제합니다.

---

## Validation

### Bean Validation

- NotBlank
- Size
- Future

### Custom Validation

직접 구현한 `@ValidUrl` Annotation을 통해

- http / https만 허용
- Host 존재 여부 확인
- 공백 포함 URL 차단
- UserInfo(URL 계정 정보) 차단

을 수행합니다.

---

## Exception Handling

Global Exception Handler를 통해 예외를 일관된 형식으로 반환합니다.

예외 종류

- URL_NOT_FOUND
- URL_EXPIRED
- SHORT_CODE_GENERATION_FAILED
- INVALID_URL_UPDATE_REQUEST

---

# API

| Method | URI | Description |
|---------|-----|-------------|
| POST | /api/urls | URL 생성 |
| GET | /{shortCode} | 원본 URL 리다이렉트 |
| GET | /api/urls | URL 목록 조회 |
| GET | /api/urls/{shortCode} | URL 상세 조회 |
| PATCH | /api/urls/{shortCode} | URL 수정 |
| DELETE | /api/urls/{shortCode} | URL 삭제 |

---

# Swagger

Swagger UI

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON

```
http://localhost:8080/v3/api-docs
```

---

# Test

작성한 테스트

- Service Test
- Controller Test
- Scheduler Test
- URL Validator Test

---

# Running

## Clone

```bash
git clone https://github.com/your-id/url-shortener.git
```

## Build

```bash
./gradlew build
```

## Run

```bash
./gradlew bootRun
```

---

# Future Improvements

- Docker
- GitHub Actions
- Redis Cache
- User Authentication (JWT)
- QR Code 생성
- URL 접근 통계

---

# What I Learned

이번 프로젝트를 통해 다음 내용을 학습했습니다.

- Spring Boot 기반 REST API 설계
- 계층형 아키텍처 구성
- Spring Validation 및 Custom Validator 구현
- Global Exception Handling
- JPA Repository 활용
- Pagination 및 검색 기능 구현
- Scheduler를 활용한 Batch 작업
- Swagger(OpenAPI) 문서화
- Mockito와 MockMvc를 활용한 테스트 코드 작성
