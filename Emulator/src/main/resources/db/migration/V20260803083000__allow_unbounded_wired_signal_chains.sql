-- Signal feedback networks may intentionally exceed the historical depth 100.
-- Zero is the explicit unlimited sentinel used by WiredEffectSendSignal.
INSERT INTO wired_emulator_settings (`key`, `value`, `comment`)
VALUES (
    'wired.signal.max.depth',
    '0',
    'Maximum Send Signal chain depth; 0 disables this limit.')
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`),
    `comment` = VALUES(`comment`);
