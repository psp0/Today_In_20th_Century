# Tomcat 배포 가이드

이 문서는 Spring Boot 백엔드 프로젝트를 Tomcat에 안전하게 배포하는 전체 과정을 단계별로 안내합니다.

## 1. 배포 준비

### 1.1 환경 변수 설정
- 프로젝트 루트(`/backend/`)에 `.env` 파일을 생성하고, 아래 환경 변수들을 실제 값으로 설정하세요.

```bash
# MySQL 설정
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/century20
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# Redis 설정 (사용 시)
REDIS_HOST=your_redis_host
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT 설정
JWT_SECRET=your_jwt_secret_key
```

### 1.2 Spring 프로필 설정
- Redis 사용 여부에 따라 아래 중 하나를 `.env`에 추가하세요.
```bash
# Redis 사용 시
SPRING_PROFILES_ACTIVE=redis
# Redis 미사용 시
SPRING_PROFILES_ACTIVE=noredis
```

### 1.3 WAR 파일 빌드
- 프로젝트 루트에서 아래 명령어로 빌드합니다.
```bash
./gradlew clean build
```
- 빌드가 완료되면 `build/libs/`에 `.war` 파일이 생성됩니다.

## 2. Tomcat 배포

### 2.1 WAR 파일 복사
- 생성된 WAR 파일을 Tomcat의 `webapps` 폴더로 복사합니다.
```bash
sudo cp build/libs/*.war /var/lib/tomcat9/webapps/
```
- 기존에 동일 이름의 WAR/폴더가 있으면 삭제 후 복사하세요.

### 2.2 Tomcat 재시작
- WAR 파일 복사 후 Tomcat을 재시작합니다.
```bash
sudo systemctl restart tomcat9
```
- 정상적으로 배포되었는지 로그(`/var/log/tomcat9/catalina.out`)를 확인하세요.

## 3. 정적 리소스(프론트엔드) 분리 운영

- 정적 리소스(html/css/js 등)는 Apache 웹서버에서 별도로 서비스합니다.
- Tomcat에는 백엔드(Spring Boot) WAR 파일만 배포합니다.
- 프론트엔드 파일은 Apache의 DocumentRoot(예: `/var/www/html/`)에 배포하세요.

## 4. 보안 및 CORS 설정

### 4.1 환경 변수 보안
- 민감 정보는 반드시 환경 변수로 관리하고, 코드 저장소에 노출되지 않도록 합니다.
- `.env` 파일을 `.gitignore`에 추가하세요.

### 4.2 Tomcat 보안 설정
- `/etc/tomcat9/server.xml`의 `<Connector>` 설정은 HTTP와 HTTPS 환경에 따라 다릅니다.

#### [HTTP 예시]
```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"
           maxPostSize="10485760"  <!-- 10MB 제한 -->
           maxHttpHeaderSize="8192"/>
```

#### [HTTPS(SSL) 예시]
```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxPostSize="10485760"
           maxHttpHeaderSize="8192"
           SSLEnabled="true"
           scheme="https"
           secure="true"
           keystoreFile="/etc/tomcat9/keystore.p12"
           keystorePass="your_keystore_password"
           keystoreType="PKCS12"
           clientAuth="false"
           sslProtocol="TLS"/>
```

**[인증서 파일 변환 및 적용 가이드]**
- 인증기관에서 받은 아래 3개 파일이 모두 필요합니다:
  - 서버 인증서: `certificate.crt` (또는 `fullchain.pem`)
  - 개인키: `private.key`
  - 체인(중간) 인증서: `ca_bundle.crt` (또는 `chain.pem`)
- 위 3개 파일을 한 폴더에 모아두세요.
- 아래 명령어로 PKCS12 keystore(`letsencrypt.p12`)로 변환합니다:
  ```bash
  openssl pkcs12 -export \
    -in certificate.crt \
    -inkey private.key \
    -out letsencrypt.p12 \
    -name tomcat \
    -CAfile ca_bundle.crt \
    -caname root
  ```
  - 명령어 실행 시 비밀번호를 입력하라는 메시지가 나오면, Tomcat 설정의 `keystorePass`에 동일하게 입력해야 합니다.
  - 변환 후 letsencrypt.p12 파일이 생성되었는지 꼭 확인하세요.
- 생성된 `letsencrypt.p12` 파일을 Tomcat 서버의 `/etc/tomcat9/` 등 원하는 경로에 복사합니다.
- Tomcat의 `<Connector>` 설정에서 `keystoreFile` 경로와 `keystorePass`를 정확히 맞춰줍니다.
- Tomcat을 재시작한 뒤, https://도메인 으로 접속해 인증서가 정상 적용됐는지 브라우저에서 확인하세요.
- 인증서 만료 시(보통 90일) 위 과정을 반복해야 하니, 명령어와 파일 위치를 기록해두세요.

### 4.3 CORS 설정
- 프론트엔드 도메인만 허용하도록 CORS를 설정합니다.
- `/etc/tomcat9/context.xml` 또는 각 앱의 `META-INF/context.xml`에 아래를 추가하세요.
```xml
<Context>
  <Valve className="org.apache.catalina.valves.CorsValve"
         allowedOrigins="https://your-frontend-domain.com"
         allowedMethods="GET,POST,PUT,DELETE,OPTIONS"
         allowedHeaders="Content-Type,Authorization"/>
</Context>
```
- 여러 도메인을 허용하려면 `allowedOrigins`에 쉼표로 구분해 추가합니다.
