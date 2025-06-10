-- MySQL 데이터베이스 및 테이블 생성 스크립트
CREATE DATABASE IF NOT EXISTS centurynews_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE centurynews_db;

-- 사용자 테이블
CREATE TABLE `user` (
    `user_no` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(50) NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NULL,
    PRIMARY KEY (`user_no`),
    UNIQUE KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 인증 테이블
CREATE TABLE `user_auth` (
    `user_no` BIGINT(20) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `pw_changed` DATETIME NULL,
    PRIMARY KEY (`user_no`),
    CONSTRAINT `fk_user_auth_user` FOREIGN KEY (`user_no`) REFERENCES `user` (`user_no`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 정보 테이블
CREATE TABLE `user_info` (
    `user_no` BIGINT(20) NOT NULL,
    `email` VARCHAR(100) NULL,
    `phone` VARCHAR(20) NULL,
    `name` VARCHAR(20) NULL,
    `nickname` VARCHAR(50) NOT NULL,
    `birth_date` DATE NOT NULL,
    `gender` CHAR(1) NULL,
    `updated_at` DATETIME NULL,
    PRIMARY KEY (`user_no`),
    CONSTRAINT `fk_user_info_user` FOREIGN KEY (`user_no`) REFERENCES `user` (`user_no`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 리프레시 토큰 테이블
CREATE TABLE `user_refresh_token` (
    `token_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_no` BIGINT(20) NOT NULL,
    `refresh_token` VARCHAR(512) NULL,
    `issued_at` DATETIME NULL,
    `expires_at` DATETIME NULL,
    `revoked` BOOLEAN DEFAULT FALSE NULL,
    PRIMARY KEY (`token_id`),
    INDEX `idx_user_refresh_token_user_no` (`user_no`),
    CONSTRAINT `fk_user_refresh_token_user` FOREIGN KEY (`user_no`) REFERENCES `user` (`user_no`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 뉴스 기사 테이블
CREATE TABLE news_articles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    main_category VARCHAR(10) NOT NULL,
    address VARCHAR(100),
    published_date DATE NOT NULL,
    press VARCHAR(20),
    reporter VARCHAR(255),
    title VARCHAR(255),
    category_level1 VARCHAR(100),
    category_level2 VARCHAR(100),
    category_level3 VARCHAR(255),
    content TEXT,
    pub_day TINYINT AS (DAY(published_date)) VIRTUAL,
    pub_month TINYINT AS (MONTH(published_date)) VIRTUAL,
    INDEX `idx_pub_dm` (`pub_month`, `pub_day`),
    INDEX `idx_cat_dm` (`main_category`, `pub_month`, `pub_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;