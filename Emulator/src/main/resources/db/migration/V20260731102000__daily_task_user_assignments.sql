CREATE TABLE IF NOT EXISTS `user_daily_task_assignments` (
    `user_id` INT NOT NULL,
    `task_date` DATE NOT NULL,
    `task_id` INT NOT NULL,
    `slot` TINYINT NOT NULL,
    `assigned_at` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`user_id`, `task_date`, `slot`),
    UNIQUE KEY `uq_user_daily_task_assignments_task` (`user_id`, `task_date`, `task_id`),
    KEY `idx_user_daily_task_assignments_task` (`task_id`),
    CONSTRAINT `fk_user_daily_task_assignments_task` FOREIGN KEY (`task_id`) REFERENCES `daily_tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
