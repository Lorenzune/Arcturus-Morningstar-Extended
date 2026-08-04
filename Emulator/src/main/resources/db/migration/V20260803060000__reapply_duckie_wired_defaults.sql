-- Reintroduces Duckie's replacement interactions after the local rollback
-- migration, while keeping rapid send/receive signal feedback unrestricted.

UPDATE items_base
SET interaction_type = 'wf_xtra_mov_curve'
WHERE interaction_type = 'wf_xtra_mov_animation';

UPDATE items_base
SET interaction_type = 'wf_act_change_opacity'
WHERE interaction_type = 'wf_act_furni_opacity';

UPDATE items i
    JOIN items_base b ON b.id = i.item_id
SET i.wired_data = JSON_OBJECT(
        'curveType', CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.animationEffect')) AS UNSIGNED),
        'intensity', CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.gravityIntensity')) AS UNSIGNED))
WHERE b.interaction_type = 'wf_xtra_mov_curve'
  AND JSON_VALID(i.wired_data)
  AND JSON_EXTRACT(i.wired_data, '$.animationEffect') IS NOT NULL;

UPDATE room_templates_items i
    JOIN items_base b ON b.id = i.item_id
SET i.wired_data = JSON_OBJECT(
        'curveType', CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.animationEffect')) AS UNSIGNED),
        'intensity', CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.gravityIntensity')) AS UNSIGNED))
WHERE b.interaction_type = 'wf_xtra_mov_curve'
  AND JSON_VALID(i.wired_data)
  AND JSON_EXTRACT(i.wired_data, '$.animationEffect') IS NOT NULL;

UPDATE items i
    JOIN items_base b ON b.id = i.item_id
SET i.wired_data = JSON_SET(
        i.wired_data,
        '$.visibility',
        1 - CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.visibility')) AS UNSIGNED),
        '$.durationSeconds',
        0,
        '$.userSource',
        0)
WHERE b.interaction_type = 'wf_act_change_opacity'
  AND JSON_VALID(i.wired_data)
  AND JSON_EXTRACT(i.wired_data, '$.visibility') IS NOT NULL
  AND JSON_EXTRACT(i.wired_data, '$.durationSeconds') IS NULL;

UPDATE room_templates_items i
    JOIN items_base b ON b.id = i.item_id
SET i.wired_data = JSON_SET(
        i.wired_data,
        '$.visibility',
        1 - CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.visibility')) AS UNSIGNED),
        '$.durationSeconds',
        0,
        '$.userSource',
        0)
WHERE b.interaction_type = 'wf_act_change_opacity'
  AND JSON_VALID(i.wired_data)
  AND JSON_EXTRACT(i.wired_data, '$.visibility') IS NOT NULL
  AND JSON_EXTRACT(i.wired_data, '$.durationSeconds') IS NULL;

-- 0 is the explicit unlimited sentinel understood by WiredExecutionGuard.
INSERT INTO wired_emulator_settings (`key`, `value`, `comment`)
VALUES (
    'wired.abuse.max.events.per.window',
    '0',
    'Maximum identical wired events inside the abuse window; 0 disables this limit.')
ON DUPLICATE KEY UPDATE
    `value` = VALUES(`value`),
    `comment` = VALUES(`comment`);
