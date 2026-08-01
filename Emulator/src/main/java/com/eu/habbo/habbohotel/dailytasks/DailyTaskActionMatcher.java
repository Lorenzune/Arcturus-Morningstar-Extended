package com.eu.habbo.habbohotel.dailytasks;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.ItemInteraction;
import com.eu.habbo.habbohotel.items.interactions.InteractionRoller;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class DailyTaskActionMatcher {
    public static final String ROLLER_DISCO = "roller_disco";
    public static final String FIND_HOTSPOT = "find_hotspot";
    public static final String FIND_MOVIE_SCREEN = "find_movie_screen";
    public static final String BUY_FROM_CATALOGUE = "buy_from_catalogue";
    public static final String MOVE_ITEM = "move_item";
    public static final String ROTATE_ITEM = "rotate_item";
    public static final String SWITCH_ITEM_STATE = "switch_item_state";
    public static final String WEAR_BADGE = "wear_badge";
    public static final String CHANGE_MOTTO = "change_motto";
    public static final String CHANGE_FIGURE = "change_figure";
    public static final String SIGN_10 = "sign_10";
    public static final String JUMP = "jump";
    public static final String DANCE_PARTY_HOST = "dance_party_host";
    public static final String LOVE_EFFECT = "love_effect";
    public static final String SKATE_EFFECT = "skate_effect";

    private static final String[] ROLLER_TERMS = {
            "roller", "skate", "skating", "iceskate", "dancefloor", "val11_floor"
    };

    private static final String[] HOTSPOT_TERMS = {
            "parasol", "umbrella", "sunshade", "lifeguard", "hotspot", "ombrell"
    };

    private static final String[] MOVIE_SCREEN_TERMS = {
            "cine_screen", "movie_screen", "movie screen", "cinema screen", "moviescreen", "film screen",
            "schermo cinematografico", "schermo cinema"
    };

    private static final ItemRule[] FURNI_USE_RULES = {
            exact("daily_1734011773612", "hblooza_bbq"),
            like("daily_1734011895115", "fball_ball"),
            like("daily_1734079966714", "boat", "bw_boat_p"),
            exact("daily_1734080389144", "hal_cauldron"),
            exact("daily_1734083261885", "sand_cstl_twr"),
            exact("daily_1734083605369", "party_djtable"),
            exact("daily_1734084263725", "bw_fin"),
            exact("daily_1734085631955", "cine_screen"),
            like("daily_1734091081760", "parasol", "paris_c15_parasol"),
            exact("daily_1735032138894", "picnic_basket"),
            exact("daily_1735032866235", "sb_rail"),
            exact("daily_1736418692846", "coralking_c18_clamshell2"),
            exact("daily_1736419490512", "bw_shower"),
            like("daily_1738669228963", "anc_trophy_"),
            exact("daily_1738669470836", "tiki_waterfall"),
            like("daily_1738670028343", "fball_ball"),
            exact("daily_1741863086174", "wf_floor_switch1"),
            interaction("daily_1742369952117", "teleport")
    };

    private static final ItemRule[] FURNI_WALK_RULES = {
            exact("daily_1734084926818", "wallchair"),
            exact("daily_1734085847884", "uni_messbed"),
            exact("daily_1735033170628", "sb_ramp"),
            exact("daily_1736418045621", "runway_chair_1"),
            exact("daily_1738670908293", "val11_floor"),
            exact("daily_1738671194164", "sb_tile"),
            exact("daily_1738672844085", "urban_sidewalk"),
            exact("daily_1742371344744", "runway_chair_1"),
            exact("daily_1742379817871", "uni_messbed")
    };

    private static final HandItemRule[] FURNI_HANDITEM_RULES = {
            new HandItemRule(43, "daily_1734084662772"),
            new HandItemRule(38, "daily_1734090080825"),
            new HandItemRule(53, "daily_1742193348839"),
            new HandItemRule(114, "daily_1742194750225"),
            new HandItemRule(76, "daily_1742195901294"),
            new HandItemRule(32, "daily_1742199338599")
    };

    private static final int LOVE_EFFECT_ID = 9;
    private static final int[] SKATE_EFFECT_IDS = { 71, 72 };

    private DailyTaskActionMatcher() {
    }

    public static void addProgress(Habbo habbo, String actionType, int amount) {
        if (habbo == null || habbo.getHabboInfo() == null) return;

        Emulator.getGameEnvironment().getRewardTrackManager()
                .addProgress(habbo.getHabboInfo().getId(), actionType, amount);
    }

    public static boolean isRollerDiscoStep(Habbo habbo, HabboItem item) {
        if (habbo == null || item == null) return false;

        return item instanceof InteractionRoller || matchesItem(item, ROLLER_TERMS);
    }

    public static void addFurniUseProgress(Habbo habbo, HabboItem item) {
        if (habbo == null || item == null) return;

        Set<String> actionTypes = new HashSet<>();
        for (ItemRule rule : FURNI_USE_RULES) {
            if (rule.matches(item)) actionTypes.add(rule.actionType);
        }

        if (isHotspotItem(item)) actionTypes.add(FIND_HOTSPOT);
        if (isMovieScreenItem(item)) actionTypes.add(FIND_MOVIE_SCREEN);

        for (String actionType : actionTypes) {
            addProgress(habbo, actionType, 1);
        }
    }

    public static void addFurniWalkProgress(Habbo habbo, HabboItem item) {
        if (habbo == null || item == null) return;

        Set<String> actionTypes = new HashSet<>();
        for (ItemRule rule : FURNI_WALK_RULES) {
            if (rule.matches(item)) actionTypes.add(rule.actionType);
        }

        if (isRollerDiscoStep(habbo, item)) actionTypes.add(ROLLER_DISCO);

        for (String actionType : actionTypes) {
            addProgress(habbo, actionType, 1);
        }
    }

    public static void addFurniHandItemProgress(Habbo habbo, int handItemId) {
        if (habbo == null || handItemId <= 0) return;

        for (HandItemRule rule : FURNI_HANDITEM_RULES) {
            if (rule.handItemId == handItemId) {
                addProgress(habbo, rule.actionType, 1);
            }
        }
    }

    public static void addEffectProgress(Habbo habbo, int effectId) {
        if (habbo == null || effectId <= 0) return;

        if (effectId == LOVE_EFFECT_ID) {
            addProgress(habbo, LOVE_EFFECT, 1);
        }

        for (int skateEffectId : SKATE_EFFECT_IDS) {
            if (effectId == skateEffectId) {
                addProgress(habbo, SKATE_EFFECT, 1);
                return;
            }
        }
    }

    public static boolean isHotspotItem(HabboItem item) {
        return item != null && matchesItem(item, HOTSPOT_TERMS);
    }

    public static boolean isMovieScreenItem(HabboItem item) {
        return item != null && matchesItem(item, MOVIE_SCREEN_TERMS);
    }

    private static boolean matchesItem(HabboItem item, String[] terms) {
        Item baseItem = item.getBaseItem();
        if (baseItem == null) return false;

        String haystack = joinSearchText(baseItem);
        if (haystack.isBlank()) return false;

        for (String term : terms) {
            if (haystack.contains(term)) return true;
        }

        return false;
    }

    private static boolean matchesExactItem(HabboItem item, String[] terms) {
        Item baseItem = item.getBaseItem();
        if (baseItem == null) return false;

        for (String value : itemValues(baseItem)) {
            if (value == null || value.isBlank()) continue;

            String normalized = value.toLowerCase(Locale.ROOT);
            for (String term : terms) {
                if (normalized.equals(term)) return true;
            }
        }

        return false;
    }

    private static boolean matchesInteraction(HabboItem item, String[] terms) {
        Item baseItem = item.getBaseItem();
        if (baseItem == null || baseItem.getInteractionType() == null) return false;

        String interaction = baseItem.getInteractionType().getName();
        if (interaction == null) return false;

        String normalized = interaction.toLowerCase(Locale.ROOT);
        for (String term : terms) {
            if (normalized.equals(term)) return true;
        }

        return false;
    }

    private static String joinSearchText(Item item) {
        StringBuilder builder = new StringBuilder();
        append(builder, item.getName());
        append(builder, item.getFullName());
        append(builder, item.getDisplayName());

        ItemInteraction interaction = item.getInteractionType();
        if (interaction != null) append(builder, interaction.getName());

        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private static String[] itemValues(Item item) {
        return new String[] { item.getName(), item.getFullName(), item.getDisplayName() };
    }

    private static void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) return;

        if (!builder.isEmpty()) builder.append(' ');
        builder.append(value);
    }

    private static ItemRule exact(String actionType, String... terms) {
        return new ItemRule(actionType, MatchMode.EXACT, terms);
    }

    private static ItemRule like(String actionType, String... terms) {
        return new ItemRule(actionType, MatchMode.LIKE, terms);
    }

    private static ItemRule interaction(String actionType, String... terms) {
        return new ItemRule(actionType, MatchMode.INTERACTION, terms);
    }

    private enum MatchMode {
        EXACT,
        LIKE,
        INTERACTION
    }

    private static final class ItemRule {
        private final String actionType;
        private final MatchMode matchMode;
        private final String[] terms;

        private ItemRule(String actionType, MatchMode matchMode, String[] terms) {
            this.actionType = actionType;
            this.matchMode = matchMode;
            this.terms = normalizeTerms(terms);
        }

        private boolean matches(HabboItem item) {
            return switch (this.matchMode) {
                case EXACT -> matchesExactItem(item, this.terms);
                case LIKE -> matchesItem(item, this.terms);
                case INTERACTION -> matchesInteraction(item, this.terms);
            };
        }
    }

    private static final class HandItemRule {
        private final int handItemId;
        private final String actionType;

        private HandItemRule(int handItemId, String actionType) {
            this.handItemId = handItemId;
            this.actionType = actionType;
        }
    }

    private static String[] normalizeTerms(String[] terms) {
        String[] normalized = new String[terms.length];
        for (int index = 0; index < terms.length; index++) {
            normalized[index] = terms[index].toLowerCase(Locale.ROOT);
        }
        return normalized;
    }
}
