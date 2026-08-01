CREATE TABLE IF NOT EXISTS `reward_tracks` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(64) NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `instructions` VARCHAR(255) NOT NULL,
    `theme` VARCHAR(32) NOT NULL DEFAULT 'blue',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1,
    `premium_enabled` TINYINT(1) NOT NULL DEFAULT 1,
    `premium_cost_credits` INT UNSIGNED NOT NULL DEFAULT 0,
    `premium_cost_diamonds` INT UNSIGNED NOT NULL DEFAULT 0,
    `sort_order` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `reward_tracks_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reward_track_tasks` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `track_id` INT UNSIGNED NOT NULL,
    `action_type` VARCHAR(64) NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `tip` VARCHAR(255) NOT NULL DEFAULT '',
    `tip_action` VARCHAR(64) NOT NULL DEFAULT '',
    `tip_button` VARCHAR(80) NOT NULL DEFAULT '',
    `sort_order` INT NOT NULL DEFAULT 0,
    `enabled` TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    KEY `reward_track_tasks_track` (`track_id`, `sort_order`),
    CONSTRAINT `reward_track_tasks_track_fk` FOREIGN KEY (`track_id`) REFERENCES `reward_tracks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reward_track_task_levels` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `task_id` INT UNSIGNED NOT NULL,
    `level` INT UNSIGNED NOT NULL,
    `target` INT UNSIGNED NOT NULL,
    `reward_points` INT UNSIGNED NOT NULL DEFAULT 10,
    PRIMARY KEY (`id`),
    UNIQUE KEY `reward_track_task_levels_level` (`task_id`, `level`),
    CONSTRAINT `reward_track_task_levels_task_fk` FOREIGN KEY (`task_id`) REFERENCES `reward_track_tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reward_track_prizes` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `track_id` INT UNSIGNED NOT NULL,
    `required_points` INT UNSIGNED NOT NULL,
    `premium` TINYINT(1) NOT NULL DEFAULT 0,
    `reward_type` VARCHAR(32) NOT NULL DEFAULT 'badge',
    `reward_code` VARCHAR(128) NOT NULL DEFAULT '',
    `reward_amount` INT UNSIGNED NOT NULL DEFAULT 1,
    `action_type` VARCHAR(64) NOT NULL DEFAULT 'use_habbicon',
    `sort_order` INT NOT NULL DEFAULT 0,
    `enabled` TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    KEY `reward_track_prizes_track` (`track_id`, `premium`, `sort_order`),
    CONSTRAINT `reward_track_prizes_track_fk` FOREIGN KEY (`track_id`) REFERENCES `reward_tracks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_reward_track_progress` (
    `user_id` INT UNSIGNED NOT NULL,
    `task_id` INT UNSIGNED NOT NULL,
    `progress` INT UNSIGNED NOT NULL DEFAULT 0,
    `updated_at` INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `task_id`),
    CONSTRAINT `user_reward_track_progress_task_fk` FOREIGN KEY (`task_id`) REFERENCES `reward_track_tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_reward_track_claims` (
    `user_id` INT UNSIGNED NOT NULL,
    `prize_id` INT UNSIGNED NOT NULL,
    `claimed_at` INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `prize_id`),
    CONSTRAINT `user_reward_track_claims_prize_fk` FOREIGN KEY (`prize_id`) REFERENCES `reward_track_prizes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_reward_track_premium` (
    `user_id` INT UNSIGNED NOT NULL,
    `track_id` INT UNSIGNED NOT NULL,
    `purchased_at` INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `track_id`),
    CONSTRAINT `user_reward_track_premium_track_fk` FOREIGN KEY (`track_id`) REFERENCES `reward_tracks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `reward_tracks`
    (`id`, `code`, `title`, `description`, `instructions`, `theme`, `enabled`, `premium_enabled`, `premium_cost_credits`, `premium_cost_diamonds`, `sort_order`)
VALUES
    (1, 'introduction', 'Introduction Track', 'Learn all about Habbo!', 'Complete tasks to earn points and unlock rewards', 'introduction', 1, 1, 0, 0, 1);

INSERT IGNORE INTO `reward_track_tasks`
    (`id`, `track_id`, `action_type`, `title`, `description`, `tip`, `tip_action`, `tip_button`, `sort_order`, `enabled`)
VALUES
    (1, 1, 'enter_other_users_room', 'Visit rooms', 'Explore rooms made by other players', 'Open the Navigator and visit different rooms to discover what people are building.', 'navigator/open', 'Open Navigator', 1, 1),
    (2, 1, 'chat_with_someone', 'Chat with users', 'Say something in rooms with other people', 'Start chatting with other users in any public room.', '', '', 2, 1),
    (3, 1, 'dance', 'Party time!', 'Start dancing while you are in a room', 'Use the dance action while you are in a room.', '', '', 3, 1),
    (4, 1, 'wave', 'Wave to users', 'Wave while you are in a room', 'Wave to another Habbo from the actions menu.', '', '', 4, 1);

INSERT IGNORE INTO `reward_track_task_levels`
    (`task_id`, `level`, `target`, `reward_points`)
VALUES
    (1, 1, 1, 10), (1, 2, 5, 20), (1, 3, 20, 30),
    (2, 1, 5, 10), (2, 2, 25, 20), (2, 3, 100, 30),
    (3, 1, 1, 20), (3, 2, 5, 25), (3, 3, 20, 35),
    (4, 1, 1, 10), (4, 2, 10, 20), (4, 3, 50, 30);

INSERT IGNORE INTO `reward_track_prizes`
    (`id`, `track_id`, `required_points`, `premium`, `reward_type`, `reward_code`, `reward_amount`, `action_type`, `sort_order`, `enabled`)
VALUES
    (1, 1, 50, 0, 'badge', 'ACH_RewardTrack1', 1, 'use_habbicon', 1, 1),
    (2, 1, 100, 0, 'currency', '5', 50, 'give_respect', 2, 1),
    (3, 1, 150, 0, 'badge', 'ACH_RewardTrack2', 1, 'follow_friend', 3, 1),
    (4, 1, 200, 0, 'currency', '0', 50, 'buy_from_catalogue', 4, 1),
    (5, 1, 250, 0, 'badge', 'ACH_RewardTrack3', 1, 'chat_with_someone', 5, 1),
    (6, 1, 50, 1, 'badge', 'ACH_RewardTrackPremium1', 1, 'premium_reward', 6, 1),
    (7, 1, 100, 1, 'currency', '5', 100, 'premium_reward', 7, 1),
    (8, 1, 150, 1, 'badge', 'ACH_RewardTrackPremium2', 1, 'premium_reward', 8, 1),
    (9, 1, 200, 1, 'currency', '0', 100, 'premium_reward', 9, 1),
    (10, 1, 250, 1, 'badge', 'ACH_RewardTrackPremium3', 1, 'premium_reward', 10, 1);
