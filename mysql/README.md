# MySQL Setup Guide

## CSV 데이터 로드

CSV 파일을 MySQL에 로드하기 위한 설정 단계입니다.

1. MySQL 파일 디렉토리 생성 및 권한 설정:
```bash
sudo mkdir -p /var/lib/mysql-files
sudo chown mysql:mysql /var/lib/mysql-files
sudo chmod 755 /var/lib/mysql-files
```

2. CSV 파일 이동 및 권한 설정:
```bash
sudo rm /var/lib/mysql-files/news_data.csv 
sudo cp ./mysql/news_data.csv /var/lib/mysql-files/
sudo chmod 644 /var/lib/mysql-files/news_data.csv
```
3. 이후 03_insert_big_data.sql을 수행

## MYSQL 서비스 문제 해결

톰캣/아파치 컨테이너에서 MYSQL(MariaDB) 서비스가 실패하는 경우의 해결 방법입니다.

1. 설정 파일 디렉토리 생성:
```bash
mkdir -p /etc/systemd/system/mariadb.service.d
```

2. 오버라이드 설정 파일 생성:
```bash
nano /etc/systemd/system/mariadb.service.d/override.conf
```

3. 다음 내용을 `override.conf` 파일에 추가:
```ini
[Service]
# MariaDB의 환경 설정을 위한 보안 설정 해제
PrivateTmp=false
ProtectSystem=false
ProtectHome=false
ProtectKernelTunables=false
ProtectKernelModules=false
ProtectControlGroups=false
```

이 설정은 컨테이너 환경에서 MariaDB가 제대로 작동하도록 보안 설정을 조정합니다.

## 데이터 출처
이 프로젝트에서 사용하는 데이터는 다음의 공공데이터포털에서 제공받은 데이터입니다 <br> 데이터를 하나의 csv에 합치어 sql이 인식할 수 있도록 전처리를 거쳐 사용하였습니다.

1. 파업 데이터
   - 출처: [한국언론진흥재단\_뉴스빅데이터\_메타데이터\_파업](https://www.data.go.kr/cmm/cmm/fileDownload.do?atchFileId=FILE_000000002923942&fileDetailSn=1)
   - 기간: 1992-2000

2. 출산율 데이터
   - 출처: [한국언론진흥재단\_뉴스빅데이터\_메타데이터\_출산율](https://www.data.go.kr/cmm/cmm/fileDownload.do?atchFileId=FILE_000000002923932&fileDetailSn=1)
   - 기간: 1992-2000

3. 기준금리 데이터
   - 출처: [한국언론진흥재단\_뉴스빅데이터\_메타데이터\_기준금리](https://www.data.go.kr/cmm/cmm/fileDownload.do?atchFileId=FILE_000000002923926&fileDetailSn=1)
   - 기간: 1993-2000

4. 가계대출 데이터
   - 출처: [한국언론진흥재단\_뉴스빅데이터\_메타데이터\_가계대출](https://www.data.go.kr/cmm/cmm/fileDownload.do?atchFileId=FILE_000000002923912&fileDetailSn=1)
   - 기간: 1992-2000

5. 물가 데이터
   - 출처: [한국언론진흥재단\_뉴스빅데이터\_메타데이터\_물가](https://www.data.go.kr/cmm/cmm/fileDownload.do?atchFileId=FILE_000000002923986&fileDetailSn=1)
   - 기간: 1992-2000
