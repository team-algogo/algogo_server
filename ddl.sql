SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE DATABASE IF NOT EXISTS `algogo_dev`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `algogo_dev`;

SET FOREIGN_KEY_CHECKS = 0;

-- FK 역순 드롭 (누락 없이 전체 포함)
DROP TABLE IF EXISTS `user_review_reactions`;
DROP TABLE IF EXISTS `required_reviews`;
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `submissions_algorithms`;
DROP TABLE IF EXISTS `submissions`;
DROP TABLE IF EXISTS `programs_problems`;
DROP TABLE IF EXISTS `group_rooms_users`;
DROP TABLE IF EXISTS `campaigns_users`;
DROP TABLE IF EXISTS `programs_users`;
DROP TABLE IF EXISTS `program_joins`;
DROP TABLE IF EXISTS `program_invites`;
DROP TABLE IF EXISTS `programs_categories`;
DROP TABLE IF EXISTS `campaigns`;
DROP TABLE IF EXISTS `group_rooms`;
DROP TABLE IF EXISTS `alarms`;
DROP TABLE IF EXISTS `alarm_types`;
DROP TABLE IF EXISTS `algorithm_keywords`;
DROP TABLE IF EXISTS `algorithms`;
DROP TABLE IF EXISTS `problems`;
DROP TABLE IF EXISTS `programs`;
DROP TABLE IF EXISTS `program_types`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1) USERS
-- =========================================================
CREATE TABLE `users` (
                         `created_at` DATETIME(6) DEFAULT NULL,
                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                         `modified_at` DATETIME(6) DEFAULT NULL,
                         `description` VARCHAR(255) NOT NULL,
                         `email` VARCHAR(255) NOT NULL,
                         `nickname` VARCHAR(255) NOT NULL,
                         `password` VARCHAR(255) NOT NULL,
                         `profile_image` VARCHAR(255) DEFAULT NULL,
                         `user_role` ENUM('ADMIN','USER') NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_user_email` (`email`),
                         UNIQUE KEY `uk_user_nickname` (`nickname`),
                         KEY `idx_users_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 2) ALARM TYPES / ALARMS
-- =========================================================
CREATE TABLE `alarm_types` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `name` VARCHAR(255) NOT NULL,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_alarm_type_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `alarms` (
                          `is_read` BIT(1) NOT NULL,
                          `alarm_type_id` BIGINT NOT NULL,
                          `created_at` DATETIME(6) DEFAULT NULL,
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `modified_at` DATETIME(6) DEFAULT NULL,
                          `user_id` BIGINT NOT NULL,
                          `message` VARCHAR(255) NOT NULL,
                          `payload` JSON DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          KEY `idx_alarms_user_read_created` (`user_id`, `is_read`, `created_at`),
                          KEY `idx_alarms_type_created` (`alarm_type_id`, `created_at`),
                          CONSTRAINT `fk_alarms_alarm_type`
                              FOREIGN KEY (`alarm_type_id`) REFERENCES `alarm_types` (`id`)
                                  ON DELETE CASCADE,
                          CONSTRAINT `fk_alarms_user`
                              FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 3) ALGORITHMS / KEYWORDS
-- =========================================================
CREATE TABLE `algorithms` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `name` VARCHAR(255) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_algorithms_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `algorithm_keywords` (
                                      `algorithm_id` BIGINT NOT NULL,
                                      `id` BIGINT NOT NULL AUTO_INCREMENT,
                                      `keyword` VARCHAR(255) NOT NULL,
                                      PRIMARY KEY (`id`),
                                      KEY `idx_algorithm_keywords_algorithm` (`algorithm_id`),
                                      KEY `idx_algorithm_keywords_keyword` (`keyword`),
                                      UNIQUE KEY `uk_algorithm_keywords_algorithm_keyword` (`algorithm_id`, `keyword`),
                                      CONSTRAINT `fk_algorithm_keywords_algorithm`
                                          FOREIGN KEY (`algorithm_id`) REFERENCES `algorithms` (`id`)
                                              ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 4) CATEGORIES / PROGRAM TYPES / PROGRAMS
-- =========================================================
CREATE TABLE `categories` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `name` VARCHAR(255) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `program_types` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `name` VARCHAR(255) NOT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_program_type_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `programs` (
                            `created_at` DATETIME(6) DEFAULT NULL,
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `modified_at` DATETIME(6) DEFAULT NULL,
                            `program_type_id` BIGINT NOT NULL,
                            `description` VARCHAR(255) NOT NULL,
                            `thumbnail` VARCHAR(255) DEFAULT NULL,
                            `title` VARCHAR(255) NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_program_title` (`title`),
                            KEY `idx_programs_program_type` (`program_type_id`),
                            KEY `idx_programs_created_at` (`created_at`),
                            CONSTRAINT `fk_programs_program_type`
                                FOREIGN KEY (`program_type_id`) REFERENCES `program_types` (`id`)
                                    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 5) PROGRAM SUBTYPES: CAMPAIGNS / GROUP_ROOMS (PK=FK)
-- =========================================================
CREATE TABLE `campaigns` (
                             `capacity` BIGINT NOT NULL,
                             `end_date` DATETIME(6) NOT NULL,
                             `id` BIGINT NOT NULL,
                             `start_date` DATETIME(6) NOT NULL,
                             PRIMARY KEY (`id`),
                             KEY `idx_campaigns_period` (`start_date`, `end_date`),
                             CONSTRAINT `fk_campaign_program`
                                 FOREIGN KEY (`id`) REFERENCES `programs` (`id`)
                                     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `group_rooms` (
                               `capacity` BIGINT NOT NULL,
                               `id` BIGINT NOT NULL,
                               PRIMARY KEY (`id`),
                               CONSTRAINT `fk_group_rooms_program`
                                   FOREIGN KEY (`id`) REFERENCES `programs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 6) PROGRAMS <-> CATEGORIES (M:N)
-- =========================================================
CREATE TABLE `programs_categories` (
                                       `category_id` BIGINT NOT NULL,
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `program_id` BIGINT NOT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY `idx_programs_categories_category` (`category_id`),
                                       KEY `idx_programs_categories_program` (`program_id`),
                                       UNIQUE KEY `uk_programs_categories_program_category` (`program_id`, `category_id`),
                                       CONSTRAINT `fk_programs_categories_program`
                                           FOREIGN KEY (`program_id`) REFERENCES `programs` (`id`)
                                               ON DELETE CASCADE,
                                       CONSTRAINT `fk_programs_categories_category`
                                           FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
                                               ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 7) PROBLEMS
-- =========================================================
CREATE TABLE `problems` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `problem_link` VARCHAR(255) NOT NULL,
                            `problem_no` VARCHAR(255) NOT NULL,
                            `title` VARCHAR(255) NOT NULL,
                            `difficulty_type` ENUM(
                                'BRONZE_1','BRONZE_2','BRONZE_3','BRONZE_4','BRONZE_5',
                                'D1','D2','D3','D4','D5','D6','D7','D8',
                                'DIAMOND_1','DIAMOND_2','DIAMOND_3','DIAMOND_4','DIAMOND_5',
                                'GOLD_1','GOLD_2','GOLD_3','GOLD_4','GOLD_5',
                                'LEVEL_0','LEVEL_1','LEVEL_2','LEVEL_3','LEVEL_4','LEVEL_5',
                                'PLATINUM_1','PLATINUM_2','PLATINUM_3','PLATINUM_4','PLATINUM_5',
                                'RUBY_1','RUBY_2','RUBY_3','RUBY_4','RUBY_5',
                                'SILVER_1','SILVER_2','SILVER_3','SILVER_4','SILVER_5',
                                'UNRATED'
                                ) NOT NULL,
                            `platform_type` ENUM('BOJ','PROGRAMMERS','SWEA') NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_problems_platform_type_problem_no` (`platform_type`,`problem_no`),
                            KEY `idx_problems_platform_difficulty` (`platform_type`, `difficulty_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 8) PROGRAM JOIN/INVITE
-- =========================================================
CREATE TABLE `program_invites` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT,
                                   `program_id` BIGINT NOT NULL,
                                   `user_id` BIGINT NOT NULL,
                                   `invite_status` ENUM('ACCEPTED','DENIED','PENDING') NOT NULL,
                                   PRIMARY KEY (`id`),
                                   KEY `idx_program_invites_program_status` (`program_id`, `invite_status`),
                                   KEY `idx_program_invites_user_status` (`user_id`, `invite_status`),
                                   CONSTRAINT `fk_program_invites_program`
                                       FOREIGN KEY (`program_id`) REFERENCES `programs` (`id`)
                                           ON DELETE CASCADE,
                                   CONSTRAINT `fk_program_invites_user`
                                       FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                           ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `program_joins` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `program_id` BIGINT NOT NULL,
                                 `user_id` BIGINT NOT NULL,
                                 `join_status` ENUM('ACCEPTED','DENIED','PENDING') NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `idx_program_joins_program_status` (`program_id`, `join_status`),
                                 KEY `idx_program_joins_user_status` (`user_id`, `join_status`),
                                 CONSTRAINT `fk_program_joins_program`
                                     FOREIGN KEY (`program_id`) REFERENCES `programs` (`id`)
                                         ON DELETE CASCADE,
                                 CONSTRAINT `fk_program_joins_user`
                                     FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 9) PROGRAMS_USERS + SUBTYPE TABLES (group_rooms_users, campaigns_users)
-- =========================================================
CREATE TABLE `programs_users` (
                                  `created_at` DATETIME(6) DEFAULT NULL,
                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `modified_at` DATETIME(6) DEFAULT NULL,
                                  `program_id` BIGINT NOT NULL,
                                  `user_id` BIGINT NOT NULL,
                                  `program_user_status` ENUM('ACTIVE','WITHDRAW') NOT NULL,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_programs_users_program_user` (`program_id`,`user_id`),
                                  KEY `idx_programs_users_user_status` (`user_id`, `program_user_status`),
                                  KEY `idx_programs_users_program` (`program_id`),
                                  CONSTRAINT `fk_programs_users_user`
                                      FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                          ON DELETE CASCADE,
                                  CONSTRAINT `fk_programs_users_program`
                                      FOREIGN KEY (`program_id`) REFERENCES `programs` (`id`)
                                          ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `group_rooms_users` (
                                     `id` BIGINT NOT NULL,
                                     `group_role` ENUM('ADMIN','MANAGER','USER') NOT NULL,
                                     PRIMARY KEY (`id`),
                                     CONSTRAINT `fk_group_user`
                                         FOREIGN KEY (`id`) REFERENCES `programs_users` (`id`)
                                             ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `campaigns_users` (
                                   `id` BIGINT NOT NULL,
                                   `penalty_cnt` BIGINT NOT NULL,
                                   PRIMARY KEY (`id`),
                                   CONSTRAINT `fk_campaigns_users_programs_users`
                                       FOREIGN KEY (`id`) REFERENCES `programs_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 10) PROGRAMS_PROBLEMS
-- =========================================================
CREATE TABLE `programs_problems` (
                                     `end_date` DATETIME(6) NOT NULL,
                                     `id` BIGINT NOT NULL AUTO_INCREMENT,
                                     `participant_count` BIGINT NOT NULL,
                                     `problem_id` BIGINT NOT NULL,
                                     `program_id` BIGINT NOT NULL,
                                     `solved_count` BIGINT NOT NULL,
                                     `start_date` DATETIME(6) NOT NULL,
                                     `submission_count` BIGINT NOT NULL,
                                     `view_count` BIGINT NOT NULL,
                                     `difficulty_view_type` ENUM('PROBLEM_DIFFICULTY','USER_DIFFICULTY') DEFAULT NULL,
                                     `user_difficulty_type` ENUM('EASY','HARD','MEDIUM') DEFAULT NULL,
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_programs_problems_program_problem` (`program_id`, `problem_id`),
                                     KEY `idx_programs_problems_program_period` (`program_id`, `start_date`, `end_date`),
                                     KEY `idx_programs_problems_problem` (`problem_id`),
                                     CONSTRAINT `fk_programs_problems_program`
                                         FOREIGN KEY (`program_id`) REFERENCES `programs` (`id`)
                                             ON DELETE CASCADE,
                                     CONSTRAINT `fk_programs_problems_problem`
                                         FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`)
                                             ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 11) SUBMISSIONS / SUBMISSIONS_ALGORITHMS
-- =========================================================
CREATE TABLE `submissions` (
                               `ai_score` DECIMAL(5,2) DEFAULT NULL,
                               `is_success` BIT(1) NOT NULL,
                               `created_at` DATETIME(6) DEFAULT NULL,
                               `exec_time` BIGINT NOT NULL,
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `memory` BIGINT NOT NULL,
                               `modified_at` DATETIME(6) DEFAULT NULL,
                               `program_problem_id` BIGINT NOT NULL,
                               `user_id` BIGINT DEFAULT NULL,
                               `view_count` BIGINT NOT NULL,
                               `ai_score_reason` TEXT,
                               `code` VARCHAR(255) NOT NULL,
                               `language` VARCHAR(255) NOT NULL,
                               `strategy` TEXT NOT NULL,
                               PRIMARY KEY (`id`),
                               KEY `idx_submissions_program_problem_created` (`program_problem_id`, `created_at`),
                               KEY `idx_submissions_user_created` (`user_id`, `created_at`),
                               CONSTRAINT `fk_submissions_program_problem`
                                   FOREIGN KEY (`program_problem_id`) REFERENCES `programs_problems` (`id`)
                                       ON DELETE CASCADE,
                               CONSTRAINT `fk_submissions_user`
                                   FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `submissions_algorithms` (
                                          `algorithm_id` BIGINT NOT NULL,
                                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                                          `submission_id` BIGINT NOT NULL,
                                          PRIMARY KEY (`id`),
                                          KEY `idx_submissions_algorithms_algorithm` (`algorithm_id`),
                                          KEY `idx_submissions_algorithms_submission` (`submission_id`),
                                          UNIQUE KEY `uk_submissions_algorithms_submission_algorithm` (`submission_id`, `algorithm_id`),
                                          CONSTRAINT `fk_submissions_algorithms_submission`
                                              FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`)
                                                  ON DELETE CASCADE,
                                          CONSTRAINT `fk_submissions_algorithms_algorithm`
                                              FOREIGN KEY (`algorithm_id`) REFERENCES `algorithms` (`id`)
                                                  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 12) REVIEWS / REQUIRED_REVIEWS / REACTIONS
-- =========================================================
CREATE TABLE `reviews` (
                           `code_line` BIGINT DEFAULT NULL,
                           `created_at` DATETIME(6) DEFAULT NULL,
                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                           `like_count` BIGINT NOT NULL,
                           `modified_at` DATETIME(6) DEFAULT NULL,
                           `parent_review_id` BIGINT DEFAULT NULL,
                           `submission_id` BIGINT NOT NULL,
                           `user_id` BIGINT DEFAULT NULL,
                           `content` TEXT NOT NULL,
                           PRIMARY KEY (`id`),
                           KEY `idx_review_submission` (`submission_id`),
                           KEY `idx_reviews_submission_created` (`submission_id`, `created_at`),
                           KEY `idx_reviews_parent` (`parent_review_id`),
                           KEY `idx_reviews_user_created` (`user_id`, `created_at`),
                           CONSTRAINT `fk_reviews_submission`
                               FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`)
                                   ON DELETE CASCADE,
                           CONSTRAINT `fk_reviews_user`
                               FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                   ON DELETE SET NULL,
                           CONSTRAINT `fk_reviews_parent`
                               FOREIGN KEY (`parent_review_id`) REFERENCES `reviews` (`id`)
                                   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `required_reviews` (
                                    `is_done` TINYINT(1) NOT NULL DEFAULT '0',
                                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                                    `subject_submission_id` BIGINT NOT NULL,
                                    `subject_user_id` BIGINT NOT NULL,
                                    `target_submission_id` BIGINT NOT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_required_review_user_submission` (`subject_user_id`,`target_submission_id`),
                                    KEY `idx_rr_subject_done` (`subject_user_id`,`is_done`),
                                    KEY `idx_required_reviews_subject_submission` (`subject_submission_id`),
                                    KEY `idx_required_reviews_target_submission` (`target_submission_id`),
                                    CONSTRAINT `fk_required_reviews_subject_user`
                                        FOREIGN KEY (`subject_user_id`) REFERENCES `users` (`id`)
                                            ON DELETE CASCADE,
                                    CONSTRAINT `fk_required_reviews_subject_submission`
                                        FOREIGN KEY (`subject_submission_id`) REFERENCES `submissions` (`id`)
                                            ON DELETE CASCADE,
                                    CONSTRAINT `fk_required_reviews_target_submission`
                                        FOREIGN KEY (`target_submission_id`) REFERENCES `submissions` (`id`)
                                            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_review_reactions` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                                         `review_id` BIGINT NOT NULL,
                                         `user_id` BIGINT NOT NULL,
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_user_review_reaction` (`user_id`,`review_id`),
                                         KEY `idx_user_review_reactions_review` (`review_id`),
                                         KEY `idx_user_review_reactions_user` (`user_id`),
                                         CONSTRAINT `fk_user_review_reactions_review`
                                             FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`)
                                                 ON DELETE CASCADE,
                                         CONSTRAINT `fk_user_review_reactions_user`
                                             FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                                 ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
