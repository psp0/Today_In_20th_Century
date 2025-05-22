LOAD DATA INFILE 'news_data.csv'
INTO TABLE news_articles
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(main_category, address, published_date, press, reporter, title, category_level1, category_level2, category_level3, content);
