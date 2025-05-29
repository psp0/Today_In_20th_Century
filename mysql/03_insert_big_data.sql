-- 1. Ensure LOCAL_INFILE is allowed
SET GLOBAL local_infile = 1;

-- 2. Tune packet/buffer sizes to handle big rows
SET GLOBAL max_allowed_packet = 1073741824;    -- 1 GB
SET SESSION net_buffer_length = 16384;         -- 16 KB

-- 3. Disable indexes & foreign-key checks for faster load
SET SESSION unique_checks = 0;
SET SESSION foreign_key_checks = 0;
SET autocommit = 0;

-- 4. Load the data in one shot
LOAD DATA LOCAL INFILE '/var/lib/mysql-files/news_data.csv'
INTO TABLE news_articles
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(main_category, address, published_date, press,
 reporter, title, category_level1, category_level2,
 category_level3, content);

-- 5. Re-enable checks and commit
COMMIT;
SET SESSION unique_checks = 1;
SET SESSION foreign_key_checks = 1;
SET autocommit = 1;
