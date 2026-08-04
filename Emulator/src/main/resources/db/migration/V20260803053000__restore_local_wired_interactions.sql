-- Restores the local Wired opacity and movement-animation interactions after
-- V20260725090000 realigned them to Duckie's replacement implementations.
--
-- Keep V20260725090000 in the migration history: it has already been applied.
-- This forward migration reverses only furniture whose class/public name still
-- identifies the local interaction, leaving genuine movement-curve furniture
-- untouched.

UPDATE items_base
SET interaction_type = 'wf_xtra_mov_animation'
WHERE interaction_type = 'wf_xtra_mov_curve'
  AND (item_name = 'wf_xtra_mov_animation' OR public_name = 'wf_xtra_mov_animation');

UPDATE items_base
SET interaction_type = 'wf_act_furni_opacity'
WHERE interaction_type = 'wf_act_change_opacity'
  AND (item_name = 'wf_act_furni_opacity' OR public_name = 'wf_act_furni_opacity');

-- Restore {animationEffect, gravityIntensity} for the local movement extra.
UPDATE items i
    JOIN items_base b ON b.id = i.item_id
SET i.wired_data = JSON_OBJECT(
        'animationEffect', CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.curveType')) AS UNSIGNED),
        'gravityIntensity', CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.intensity')) AS UNSIGNED))
WHERE b.interaction_type = 'wf_xtra_mov_animation'
  AND JSON_VALID(i.wired_data)
  AND JSON_EXTRACT(i.wired_data, '$.curveType') IS NOT NULL;

UPDATE room_templates_items i
    JOIN items_base b ON b.id = i.item_id
SET i.wired_data = JSON_OBJECT(
        'animationEffect', CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.curveType')) AS UNSIGNED),
        'gravityIntensity', CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.intensity')) AS UNSIGNED))
WHERE b.interaction_type = 'wf_xtra_mov_animation'
  AND JSON_VALID(i.wired_data)
  AND JSON_EXTRACT(i.wired_data, '$.curveType') IS NOT NULL;

-- Restore the local visibility meaning and remove fields unsupported by the
-- local opacity effect. Other shared settings retain their existing values.
UPDATE items i
    JOIN items_base b ON b.id = i.item_id
SET i.wired_data = JSON_REMOVE(
        JSON_SET(
            i.wired_data,
            '$.visibility',
            1 - CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.visibility')) AS UNSIGNED)),
        '$.durationSeconds',
        '$.userSource')
WHERE b.interaction_type = 'wf_act_furni_opacity'
  AND JSON_VALID(i.wired_data)
  AND JSON_EXTRACT(i.wired_data, '$.visibility') IS NOT NULL
  AND JSON_EXTRACT(i.wired_data, '$.durationSeconds') IS NOT NULL;

UPDATE room_templates_items i
    JOIN items_base b ON b.id = i.item_id
SET i.wired_data = JSON_REMOVE(
        JSON_SET(
            i.wired_data,
            '$.visibility',
            1 - CAST(JSON_UNQUOTE(JSON_EXTRACT(i.wired_data, '$.visibility')) AS UNSIGNED)),
        '$.durationSeconds',
        '$.userSource')
WHERE b.interaction_type = 'wf_act_furni_opacity'
  AND JSON_VALID(i.wired_data)
  AND JSON_EXTRACT(i.wired_data, '$.visibility') IS NOT NULL
  AND JSON_EXTRACT(i.wired_data, '$.durationSeconds') IS NOT NULL;
