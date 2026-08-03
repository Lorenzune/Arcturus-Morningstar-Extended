CREATE TABLE IF NOT EXISTS `daily_tasks` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(64) NOT NULL,
    `difficulty` VARCHAR(16) NOT NULL DEFAULT 'easy',
    `action_type` VARCHAR(64) NOT NULL,
    `title` VARCHAR(128) NOT NULL,
    `description` VARCHAR(255) NOT NULL DEFAULT '',
    `target` INT NOT NULL DEFAULT 1,
    `reward_type` VARCHAR(32) NOT NULL DEFAULT 'currency',
    `reward_code` VARCHAR(64) NOT NULL DEFAULT '0',
    `reward_amount` INT NOT NULL DEFAULT 10,
    `go_link` VARCHAR(255) NOT NULL DEFAULT '',
    `sort_order` INT NOT NULL DEFAULT 0,
    `enabled` TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_daily_tasks_code` (`code`),
    KEY `idx_daily_tasks_difficulty` (`difficulty`, `enabled`, `sort_order`),
    KEY `idx_daily_tasks_action` (`action_type`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_daily_task_progress` (
    `user_id` INT NOT NULL,
    `task_id` INT NOT NULL,
    `task_date` DATE NOT NULL,
    `progress` INT NOT NULL DEFAULT 0,
    `accepted_at` INT NOT NULL DEFAULT 0,
    `updated_at` INT NOT NULL DEFAULT 0,
    `completed_at` INT NOT NULL DEFAULT 0,
    `reward_claimed_at` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `task_date`),
    KEY `idx_user_daily_task_progress_task` (`task_id`),
    CONSTRAINT `fk_user_daily_task_progress_task` FOREIGN KEY (`task_id`) REFERENCES `daily_tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `daily_tasks`
(`code`, `difficulty`, `action_type`, `title`, `description`, `target`, `reward_type`, `reward_code`, `reward_amount`, `go_link`, `sort_order`, `enabled`)
VALUES
('easy_visit_room', 'easy', 'enter_other_users_room', 'Visit a room', 'Explore a room made by another player.', 1, 'currency', '0', 10, 'navigator/show', 10, 1),
('easy_chat', 'easy', 'chat_with_someone', 'Chat with users', 'Say something in a room.', 3, 'currency', '0', 10, '', 20, 1),
('easy_wave', 'easy', 'wave', 'Wave to users', 'Wave while you are in a room.', 1, 'currency', '0', 10, '', 30, 1),
('hard_habbicon', 'hard', 'use_habbicon', 'Use a Habbicon', 'Send Habbicons while chatting.', 3, 'currency', '0', 25, '', 10, 1),
('hard_respect', 'hard', 'give_respect', 'Give respect', 'Give respect to other players.', 2, 'currency', '5', 5, '', 20, 1),
('hard_catalogue', 'hard', 'buy_from_catalogue', 'Buy from the catalogue', 'Purchase an item from the catalogue.', 1, 'credits', '', 50, 'catalog/open', 30, 1);
