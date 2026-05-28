-- Fortune Wheel
-- Tables are also created at boot by WheelManager (CREATE TABLE IF NOT EXISTS),
-- so applying this file is only needed to seed prizes + settings.

CREATE TABLE IF NOT EXISTS `wheel_prizes` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `type` VARCHAR(16) NOT NULL DEFAULT 'nothing',   -- item | badge | credits | points | spin | nothing
    `value` VARCHAR(64) NOT NULL DEFAULT '',          -- item: base item id ; badge: badge code ; others: unused
    `amount` INT(11) NOT NULL DEFAULT 1,              -- item qty / credits / points / extra spins
    `points_type` INT(11) NOT NULL DEFAULT 5,         -- for type=points (diamond default 5)
    `weight` INT(11) NOT NULL DEFAULT 1,              -- relative probability
    `label` VARCHAR(64) NOT NULL DEFAULT '',          -- slice label override (optional)
    `enabled` TINYINT(1) NOT NULL DEFAULT 1,
    `sort_order` INT(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `wheel_user_state` (
    `user_id` INT(11) NOT NULL,
    `free_spins` INT(11) NOT NULL DEFAULT 0,          -- remaining free spins for the current day
    `extra_spins` INT(11) NOT NULL DEFAULT 0,         -- bought / won spins
    `last_reset` INT(11) NOT NULL DEFAULT 0,          -- day index of last daily reset (unix / 86400)
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `wheel_recent_wins` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `user_id` INT(11) NOT NULL,
    `username` VARCHAR(64) NOT NULL DEFAULT '',
    `look` VARCHAR(255) NOT NULL DEFAULT '',
    `prize_label` VARCHAR(64) NOT NULL DEFAULT '',
    `won_at` INT(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_wheel_recent_wins_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `emulator_settings` (`key`, `value`, `comment`) VALUES
    ('wheel.free_spins_per_day', '1',  'Fortune wheel: free spins granted each day.')
    ON DUPLICATE KEY UPDATE `comment` = VALUES(`comment`);
INSERT INTO `emulator_settings` (`key`, `value`, `comment`) VALUES
    ('wheel.spin_cost', '50',          'Fortune wheel: cost of one extra spin.')
    ON DUPLICATE KEY UPDATE `comment` = VALUES(`comment`);
INSERT INTO `emulator_settings` (`key`, `value`, `comment`) VALUES
    ('wheel.spin_cost_type', '5',      'Fortune wheel: currency type for the spin cost (5 = diamonds; -1 = credits).')
    ON DUPLICATE KEY UPDATE `comment` = VALUES(`comment`);

-- Example prizes (currency / spin / nothing don't reference furniture ids).
-- Add `item`/`badge` rows with your own ids: e.g.
--   INSERT INTO wheel_prizes (type, value, amount, weight, label, sort_order) VALUES ('item','<base_item_id>',1,5,'Raro',1);
--   INSERT INTO wheel_prizes (type, value, amount, weight, label, sort_order) VALUES ('badge','<BADGE_CODE>',1,5,'Distintivo',2);
INSERT INTO `wheel_prizes` (`type`, `amount`, `points_type`, `weight`, `label`, `sort_order`) VALUES
    ('points',   25, 5, 20, '25 diamanti',  10),
    ('points',   50, 5, 12, '50 diamanti',  11),
    ('points',  200, 5,  3, '200 diamanti', 12),
    ('credits', 100, 0, 15, '100 crediti',  13),
    ('spin',      1, 0, 15, '1 Giro Extra', 14),
    ('spin',      2, 0,  6, '2 Giri Extra', 15),
    ('nothing',   0, 0, 29, 'Nulla',        16);
