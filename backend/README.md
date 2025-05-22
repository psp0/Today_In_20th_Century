# 20세기 오늘의 뉴스 - 백엔드

이 프로젝트는 20세기 주요 뉴스를 제공하는 Spring Boot 기반 백엔드 서버입니다. RESTful API를 통해 뉴스 데이터, 사용자 인증, 캐싱 등 핵심 비즈니스 로직을 제공합니다.

## 주요 기능
- 뉴스 및 사용자 데이터 관리 API
- JWT 기반 인증/인가 및 보안
- Redis 캐시(토큰, 뉴스, 사용자 정보)
- Swagger API 문서 제공(`/swagger-ui/index.html`)

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
│       ├── application-redis.yml
│       └── application-noredis.yml
└── test/                       # 테스트 코드
```

## 기술 스택
- Spring Boot 2.7.17
- MySQL 8.0.32
- Redis (선택적 캐시)
- Spring Security, JWT
- Lombok, Springfox (Swagger)

## 환경 변수 및 설정
- 환경 변수 예시: `.env.example` 참고
- 주요 변수: DB 접속 정보, JWT 시크릿, Redis 설정, `SPRING_PROFILES_ACTIVE`
- 상세 배포/보안: [`DEPLOY_TOMCAT.md`](DEPLOY_TOMCAT.md) 참고

## 실행 방법
1. 의존성 설치
   ```bash
   ./gradlew build
   ```
2. 환경 변수 설정(상세 내용은 위의 "환경 변수 및 설정" 참고)
3. 데이터베이스 준비(`mysql/`의 스키마 사용)
4. 서버 실행
   ```bash
   ./gradlew bootRun
   ```
   - Redis 사용: `SPRING_PROFILES_ACTIVE=redis`
   - 미사용: `SPRING_PROFILES_ACTIVE=noredis`

## 테스트
```bash
./gradlew test
```

## 배포
- 빌드된 WAR 파일을 Tomcat 서버에 배포
- 자세한 절차 및 체크리스트는 [`DEPLOY_TOMCAT.md`](DEPLOY_TOMCAT.md) 참고

## 보안
- JWT 인증, CORS, Redis 세션, Spring Security 적용
