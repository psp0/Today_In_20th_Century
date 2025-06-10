-- 1. Enable LOCAL_INFILE temporarily and set safe limits
SET @original_local_infile = @@GLOBAL.local_infile;
SET GLOBAL local_infile = 1;

-- 2. Set packet and buffer sizes
SET @original_max_allowed_packet = @@GLOBAL.max_allowed_packet;
SET @original_net_buffer_length = @@GLOBAL.net_buffer_length;
SET GLOBAL max_allowed_packet = 1073741824;  -- 1 GB
SET GLOBAL net_buffer_length = 16384;        -- 16 KB

-- 3. Start transaction and disable checks
START TRANSACTION;
SET unique_checks = 0;
SET foreign_key_checks = 0;
SET autocommit = 0;

-- 4. Load the CSV data into the table
LOAD DATA INFILE '/docker-entrypoint-initdb.d/cleaned_news_data.tsv'
-- LOAD DATA  LOCAL INFILE '/var/lib/mysql-files/cleaned_news_data.tsv' 
INTO TABLE news_articles
CHARACTER SET utf8mb4
FIELDS TERMINATED BY '\t'  -- 쉼표 대신 탭으로 변경
ENCLOSED BY ''              -- 따옴표 제거 (TSV는 보통 따옴표 없음)
ESCAPED BY ''               -- 이스케이프 문자 제거
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(main_category, address, published_date, press,
 reporter, title, category_level1, category_level2,
 category_level3, content);

-- 5. Check for warnings
SHOW WARNINGS;

-- 6. Re-enable checks and commit
SET unique_checks = 1;
SET foreign_key_checks = 1;
COMMIT;
SET autocommit = 1;

-- 7. Restore original GLOBAL variables
SET GLOBAL local_infile = @original_local_infile;
SET GLOBAL max_allowed_packet = @original_max_allowed_packet;
SET GLOBAL net_buffer_length = @original_net_buffer_length;
