INSERT INTO `daily_tasks`
(`code`, `difficulty`, `action_type`, `title`, `description`, `target`, `reward_type`, `reward_code`, `reward_amount`, `go_link`, `sort_order`, `enabled`)
VALUES
('1734011895115_G', 'easy', 'daily_1734011895115', 'Una partita?', 'Fai doppio clic sul pallone fino al completamento della missione.', 1, 'currency', '0', 10, '', 48, 1),
('1742198483809_G', 'easy', 'scratch_pet', 'Gratta-gratta!', 'Gratta tre volte un pet per completare la missione.', 3, 'currency', '0', 10, '', 49, 1)
ON DUPLICATE KEY UPDATE
    `difficulty` = VALUES(`difficulty`),
    `action_type` = VALUES(`action_type`),
    `title` = VALUES(`title`),
    `description` = VALUES(`description`),
    `target` = VALUES(`target`),
    `reward_type` = VALUES(`reward_type`),
    `reward_code` = VALUES(`reward_code`),
    `reward_amount` = VALUES(`reward_amount`),
    `go_link` = VALUES(`go_link`),
    `sort_order` = VALUES(`sort_order`),
    `enabled` = VALUES(`enabled`);
