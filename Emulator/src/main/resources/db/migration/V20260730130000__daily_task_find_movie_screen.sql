INSERT INTO `daily_tasks`
(`code`, `difficulty`, `action_type`, `title`, `description`, `target`, `reward_type`, `reward_code`, `reward_amount`, `go_link`, `sort_order`, `enabled`)
VALUES
('FINDMOVIESCREEN', 'easy', 'find_movie_screen', 'Sei una Star!', 'Trova un Cinema e rilassati cliccando 2 volte sullo Schermo.', 1, 'currency', '0', 10, '', 4, 1)
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
