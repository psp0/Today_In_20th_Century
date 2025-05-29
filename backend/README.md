# 20세기 오늘의 뉴스 - 백엔드

이 프로젝트는 20세기 주요 뉴스를 제공하는 Spring Boot 기반 백엔드 서버입니다. RESTful API를 통해 뉴스 데이터, 사용자 인증, 캐싱 등 핵심 비즈니스 로직을 제공합니다.

## 주요 기능
- 뉴스 및 사용자 데이터 관리
- JWT 기반 인증/인가
- Swagger API 문서 제공 (`/swagger-ui/index.html`)

## 폴더 구조
```
src/
├── main/
│   ├── java/
│   │   └── place/run/mep/century20/
│   │       ├── config/         # 설정 (JWT, Redis, Security 등)
│   │       ├── controller/     # REST API 컨트롤러
│   │       ├── dto/            # 데이터 전송 객체
│   │       ├── entity/         # JPA 엔티티
│   │       ├── exception/      # 예외 처리
│   │       ├── repository/     # 데이터 접근 계층
│   │       ├── service/        # 비즈니스 로직
│   │       └── Century20Application.java  # 진입점
│   └── resources/
│       ├── application.yml
│       └── application-noredis.yml
└── test/                       # 테스트 코드
```

## 기술 스택
- Spring Boot 3.x
- Spring Security with JWT
- Lombok, Springfox Swagger

## 환경 설정
- 환경 변수: `.env.web.example` 참고
- 주요 변수: DB 접속 정보, JWT 시크릿, Redis 설정, `SPRING_PROFILES_ACTIVE`
    
## 보안
- JWT 기반 인증/인가
- CORS 설정
- Spring Security 적용
- 비밀번호 암호화
