# 20세기 오늘의 뉴스

오늘 20세기 한국에서는 어떤 뉴스가 있었는지 확인하고 날짜와 카테고리를 선택하여 20세기 뉴스들을 탐색할 수 있는 웹 애플리케이션입니다.

## 프로젝트 구조

```
.
├── frontend/                # JavaScript, HTML, CSS 기반 프론트엔드
├── backend/                 # Java Spring Boot 기반 백엔드
├── mysql/                   # MySQL 스키마 및 데이터
├── nginx/                   # Nginx 설정
├── prometheus/              # 모니터링 설정
├── docker-compose.web.yml   # 웹 애플리케이션 Docker 설정
├── docker-compose.arm-redis.yml  # Redis Docker 설정
└── docker-compose.arm-monitoring.yml  # 모니터링 Docker 설정
```

## 인프라 구조

- **데이터베이스**: VMware 가상머신에 TurnKey Linux MySQL 이미지 사용
- **웹 애플리케이션, Redis, 모니터링**: Docker Compose를 사용하여 컨테이너화
  - 프론트엔드와 백엔드는 `docker-compose.web.yml`로 관리
  - Redis와 모니터링은 ARM 아키텍처 컨테이너 사용
  - 각각 `docker-compose.arm-redis.yml`,`docker-compose.arm-monitoring.yml`로 관리
  - Redis는 클러스터 구성 (마스터/슬레이브 설정)
  - 모니터링은 Prometheus와 Grafana 기반


## 기술 스택

- **백엔드**: Java Spring Boot
- **프론트엔드**: 순수 JavaScript, HTML, CSS
- **데이터베이스**: MySQL
- **캐시**: Redis (선택적)


## 주요 기능

- 20세기 뉴스 탐색 및 검색
- 사용자 인증 및 프로필 관리 (JWT)
- Redis 기반 캐시 (선택적)

## 실행 방법

### 환경 설정
프로젝트는 여러 환경 설정 파일을 사용합니다:
- `.env.example.webnode`: 웹 애플리케이션 환경 설정
- `.env.example.redisnode`: Redis 환경 설정
- `.env.example.monitoringnode`: 모니터링 환경 설정

각 노드에 맞게 설정 파일을 복사하여 실제 환경에 맞게 수정해야 합니다:
```bash
cp .env.example.webnode .env
```
```bash
cp .env.example.redisnode .env
```
```bash
cp .env.example.monitoringnode .env
```

**주의:** .env.example.webnode의 경우 Redis 프로필 설정
Redis 사용 여부에 따라 프로필을 설정하세요
```bash
# Redis 사용
SPRING_PROFILES_ACTIVE=redis
# Redis 미사용
SPRING_PROFILES_ACTIVE=noredis
```

### Docker 실행
프로젝트는 Docker Compose를 사용하여 실행됩니다:

1. **웹 애플리케이션 실행**
```bash
docker-compose -f docker-compose.web.yml up -d
```

2. **Redis 실행**
```bash
docker-compose -f docker-compose.arm-redis.yml up -d
```

3. **모니터링 실행**
```bash
docker-compose -f docker-compose.arm-monitoring.yml up -d
```

## Redis 캐시 기능

- **JWT 토큰 캐시**: 토큰 검증 및 사용자 매핑 성능 향상
- **뉴스 데이터 캐시**: 날짜별/자주 조회 뉴스 캐싱
- **사용자 정보 캐시**: 프로필 정보 조회 성능 향상

## Redis 클러스터 구성 가이드

Redis 클러스터를 구성할때 슬레이브 노드의 경우 .env.redis의 환경 변수를 추가로 설정해야 합니다:

### 슬레이브 노드 설정
```bash
REDIS_REPLICA_OF=master_node_ip:6379
```

### 클러스터 구성 절차
1. 모든 노드에서 Redis를 시작:
```bash
docker-compose -f docker-compose.arm-redis.yml up -d
```

2. 마스터 노드에서 클러스터를 생성:
```bash
docker exec redis redis-cli --cluster create \
    master1_ip:6379 \
    master2_ip:6379 \
    master3_ip:6379 \
    slave1_ip:6379 \
    --cluster-replicas 1 \
    --cluster-yes \
    -a yourpassword
```

**주의:** `yourpassword`를 실제로 사용할 Redis 비밀번호로 대체해야 합니다. 이 비밀번호는 `.env.redis` 파일의 `REDIS_PASSWORD` 값과 일치해야 합니다.

## 모니터링

프로젝트는 Prometheus와 Grafana를 사용하여 모니터링 기능을 제공합니다:

- **프로메테우스**: 시계열 데이터 수집 및 저장
- **그라피나**: 시각화 대시보드 제공

모니터링 설정은 `prometheus/` 디렉토리에 있으며, Docker Compose를 통해 쉽게 실행할 수 있습니다.

## 보안

- **인증/인가**: 
  - JWT 기반 인증/인가
  - Redis를 통한 토큰 캐싱 (선택적)
- **API 보안**: Spring Security 적용
- **CORS**: 적절한 설정으로 크로스 도메인 요청 관리

## 참고 문서

- [backend/README.md](backend/README.md): 백엔드 상세 설명
- [mysql/README.md](mysql/README.md): 데이터 설명

## 라이선스

MIT License
