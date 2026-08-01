INSERT INTO `daily_tasks`
(`code`, `difficulty`, `action_type`, `title`, `description`, `target`, `reward_type`, `reward_code`, `reward_amount`, `go_link`, `sort_order`, `enabled`)
VALUES
('1738670908293_G', 'easy', 'roller_disco', 'ROLLERDISCO!', 'Ce la fai a pattinare su 20 Caselle?', 20, 'currency', '0', 20, '', 1, 1),
('1734091081760_G', 'easy', 'find_hotspot', 'Hot Spots', 'Cerca un ombrellone colorato, perfetto per bloccare i raggi  del sole!', 1, 'currency', '0', 10, '', 2, 1),
('1742198483809_G', 'easy', 'scratch_pet', 'Gratta-gratta!', 'Mostra affetto verso il tuo cucciolo o quello di un amico!', 1, 'currency', '0', 10, '', 3, 1)
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
