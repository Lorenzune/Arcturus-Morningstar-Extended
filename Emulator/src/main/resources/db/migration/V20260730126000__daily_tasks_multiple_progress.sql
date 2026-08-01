ALTER TABLE `user_daily_task_progress`
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`user_id`, `task_id`, `task_date`);
