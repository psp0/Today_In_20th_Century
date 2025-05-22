# 20세기 오늘의 뉴스 - 프론트엔드

이 디렉토리는 JavaScript, HTML, CSS만으로 구현된 SPA(싱글페이지앱) 프론트엔드입니다. React 등 프레임워크, 빌드 도구 없이 직접 구현된 사용자 인터페이스와 클라이언트 로직을 담당합니다.

## 폴더 구조

```
frontend/
├── index.html
├── script.js
├── styles.css
└── README.md
```

## 기술 스택

- **JavaScript (ES6+)**
- **HTML5**
- **CSS3**

## 개발 및 실행 방법

1. `index.html`, `script.js`, `styles.css`를 브라우저에서 직접 열어 실행합니다.
   - 별도의 빌드, 번들, 패키지 설치 과정이 필요하지 않습니다.
   - 개발 서버 없이 파일을 더블클릭하거나, 간단한 로컬 서버(예: `python3 -m http.server`)로 확인할 수 있습니다.

## 서버 배포(업로드) 방법

### 1. 배포 파일 준비
- `frontend/` 폴더의 `index.html`, `script.js`, `styles.css` 파일을 준비합니다.

### 2. 서버 업로드
- **Apache 웹서버**의 DocumentRoot(예: `/var/www/html/`)에 위 파일들을 복사합니다.
- 기존 파일이 있다면 덮어씁니다.

```bash
sudo cp index.html script.js styles.css /var/www/html/
```

### 3. SPA 라우팅 지원 (선택)
- SPA의 경우 새로고침 시 404 방지를 위해 `.htaccess` 파일을 `/var/www/html/`에 추가합니다.

```
<IfModule mod_rewrite.c>
  RewriteEngine On
  RewriteBase /
  RewriteRule ^index\.html$ - [L]
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteRule . /index.html [L]
</IfModule>
```

### 4. 서버에서 정상 서비스 확인
- 웹브라우저에서 서버 주소(예: http://서버IP/)로 접속해 정상적으로 화면이 뜨는지 확인합니다.

## 환경 변수 및 추가 설정
- API 서버 주소 등 환경변수가 필요하다면, `src/services/api.js` 등에서 직접 수정하거나, JS 코드 내에서 관리합니다.
- CORS, HTTPS 등은 서버(Apache) 설정에서 별도 적용해야 합니다.

---

- 서버 환경, DocumentRoot 경로, 업로드 방식(FTP, SCP 등)은 실제 운영 환경에 맞게 조정하세요.
- Apache 외 Nginx 등 다른 웹서버도 정적 파일 서비스 방식은 유사합니다.
