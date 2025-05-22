# 20세기 오늘의 뉴스

오늘 20세기 **한국**에서는 어떤 뉴스가 있었는지 확인하고 뉴스들을 탐색할 수 있는 웹 애플리케이션입니다.

## 프로젝트 구성

```
.
├── frontend/     # 프론트엔드 (JavaScript, HTML, CSS 기반 SPA)
├── backend/      # 백엔드 (Java Spring Boot)
├── mysql/        # MySQL 스키마 및 데이터
└── README.md     # 프로젝트 설명서
```

## 기술 스택

- **백엔드**: Java Spring Boot
- **프론트엔드**: JavaScript, HTML, CSS (SPA)
- **데이터베이스**: MySQL
- **애플리케이션 서버**: Tomcat
- **캐시**: Redis (선택적)

## 주요 기능

- 오늘의 20세기 뉴스 탐색 및 검색
- 사용자 인증(JWT) 및 프로필 관리
- Redis 기반 캐시(토큰, 뉴스, 사용자 정보)

## 실행 및 배포 요약

1. **DB 준비**: `mysql/`의 스키마로 MySQL 데이터베이스 생성
2. **백엔드**: `backend/`에서 Spring Boot 실행 (Redis 사용 여부는 프로필로 선택)
   - **톰캣(Tomcat) 서버에 배포**: 백엔드 애플리케이션은 Tomcat에서 실행되도록 설정
3. **프론트엔드**: `frontend/`에서 빌드 후 결과물을 웹서버 DocumentRoot에 복사
   - **아파치(Apache) 웹서버에 배포**: 프론트엔드 정적 파일은 Apache에서 서비스

## Redis 캐시 기능

- **JWT 토큰 캐시**: 토큰 검증 및 사용자 매핑 성능 향상
- **뉴스 데이터 캐시**: 날짜별/자주 조회 뉴스 캐싱
- **사용자 정보 캐시**: 프로필 정보 조회 성능 향상

## VM 환경 및 배포 이미지

- TurnKey Linux Tomcat Apache ISO 이미지를 사용하여 VM 환경을 구성
- Apache는 프론트엔드 정적 파일 서비스, Tomcat은 백엔드(Spring Boot) 서비스 담당

## 참고 문서

- [backend/README.md](backend/README.md): 백엔드 상세 설명
- [frontend/README.md](frontend/README.md): 프론트엔드 상세 설명

## 라이선스

MIT License
