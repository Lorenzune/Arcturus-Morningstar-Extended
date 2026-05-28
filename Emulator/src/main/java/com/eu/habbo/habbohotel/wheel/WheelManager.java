package com.eu.habbo.habbohotel.wheel;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WheelManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WheelManager.class);
    private static final int RECENT_KEEP = 50;
    private static final int SECONDS_PER_DAY = 86400;

    private final List<WheelPrize> prizes = new ArrayList<>();
    private int totalWeight = 0;
    private int freeSpinsPerDay = 1;
    private int spinCost = 50;
    private int spinCostType = 5;

    public WheelManager() {
        long millis = System.currentTimeMillis();
        this.createTables();
        this.reload();
        LOGGER.info("Wheel Manager -> Loaded! ({} MS)", System.currentTimeMillis() - millis);
    }

    public void reload() {
        this.loadSettings();
        this.loadPrizes();
    }

    private void createTables() {
        final String[] ddl = {
                "CREATE TABLE IF NOT EXISTS `wheel_prizes` (" +
                        "`id` INT(11) NOT NULL AUTO_INCREMENT, `type` VARCHAR(16) NOT NULL DEFAULT 'nothing', " +
                        "`value` VARCHAR(64) NOT NULL DEFAULT '', `amount` INT(11) NOT NULL DEFAULT 1, " +
                        "`points_type` INT(11) NOT NULL DEFAULT 5, `weight` INT(11) NOT NULL DEFAULT 1, " +
                        "`label` VARCHAR(64) NOT NULL DEFAULT '', `enabled` TINYINT(1) NOT NULL DEFAULT 1, " +
                        "`sort_order` INT(11) NOT NULL DEFAULT 0, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
                "CREATE TABLE IF NOT EXISTS `wheel_user_state` (" +
                        "`user_id` INT(11) NOT NULL, `free_spins` INT(11) NOT NULL DEFAULT 0, " +
                        "`extra_spins` INT(11) NOT NULL DEFAULT 0, `last_reset` INT(11) NOT NULL DEFAULT 0, " +
                        "PRIMARY KEY (`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
                "CREATE TABLE IF NOT EXISTS `wheel_recent_wins` (" +
                        "`id` INT(11) NOT NULL AUTO_INCREMENT, `user_id` INT(11) NOT NULL, " +
                        "`username` VARCHAR(64) NOT NULL DEFAULT '', `look` VARCHAR(255) NOT NULL DEFAULT '', " +
                        "`prize_label` VARCHAR(64) NOT NULL DEFAULT '', `won_at` INT(11) NOT NULL DEFAULT 0, " +
                        "PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        };

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            for (String query : ddl) {
                statement.execute(query);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to create fortune wheel tables", e);
        }
    }

    private void loadSettings() {
        this.freeSpinsPerDay = Emulator.getConfig().getInt("wheel.free_spins_per_day", 1);
        this.spinCost = Emulator.getConfig().getInt("wheel.spin_cost", 50);
        this.spinCostType = Emulator.getConfig().getInt("wheel.spin_cost_type", 5);
    }

    private void loadPrizes() {
        this.prizes.clear();
        this.totalWeight = 0;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM wheel_prizes WHERE enabled = 1 ORDER BY sort_order ASC, id ASC");
             ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                WheelPrize prize = new WheelPrize(set);
                this.prizes.add(prize);
                this.totalWeight += prize.weight;
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load fortune wheel prizes", e);
        }
    }

    public List<WheelPrize> getPrizes() {
        return this.prizes;
    }

    public int getSpinCost() {
        return this.spinCost;
    }

    public int getSpinCostType() {
        return this.spinCostType;
    }

    private int today() {
        return Emulator.getIntUnixTimestamp() / SECONDS_PER_DAY;
    }

    // Reads the user's spin balance, applying the lazy daily reset and creating the row if missing.
    public WheelUserState getUserState(int userId) {
        WheelUserState state = new WheelUserState();
        boolean exists = false;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT free_spins, extra_spins, last_reset FROM wheel_user_state WHERE user_id = ?")) {
            statement.setInt(1, userId);
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    state.freeSpins = set.getInt("free_spins");
                    state.extraSpins = set.getInt("extra_spins");
                    state.lastReset = set.getInt("last_reset");
                    exists = true;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to read wheel state for user {}", userId, e);
        }

        int today = this.today();
        if (!exists) {
            state.freeSpins = this.freeSpinsPerDay;
            state.extraSpins = 0;
            state.lastReset = today;
            this.persistUserState(userId, state);
        } else if (state.lastReset != today) {
            state.freeSpins = this.freeSpinsPerDay;
            state.lastReset = today;
            this.persistUserState(userId, state);
        }

        return state;
    }

    private void persistUserState(int userId, WheelUserState state) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO wheel_user_state (user_id, free_spins, extra_spins, last_reset) VALUES (?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE free_spins = VALUES(free_spins), extra_spins = VALUES(extra_spins), last_reset = VALUES(last_reset)")) {
            statement.setInt(1, userId);
            statement.setInt(2, state.freeSpins);
            statement.setInt(3, state.extraSpins);
            statement.setInt(4, state.lastReset);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to persist wheel state for user {}", userId, e);
        }
    }

    // Consumes a spin (free first, then extra), picks a weighted prize, grants it and records the win.
    // Returns the prize, or null if the user has no spins or no prizes are configured.
    public synchronized WheelPrize spin(Habbo habbo) {
        int userId = habbo.getHabboInfo().getId();
        WheelUserState state = this.getUserState(userId);

        boolean usedFree;
        if (state.freeSpins > 0) {
            state.freeSpins--;
            usedFree = true;
        } else if (state.extraSpins > 0) {
            state.extraSpins--;
            usedFree = false;
        } else {
            return null;
        }

        WheelPrize prize = this.pickWeighted();
        if (prize == null) {
            // No prizes configured — refund the spin we just consumed.
            if (usedFree) state.freeSpins++; else state.extraSpins++;
            return null;
        }

        this.giveReward(habbo, prize, state);
        this.persistUserState(userId, state);

        // Record every spin (including "nothing") so the live feed shows all activity.
        this.recordWin(habbo, prize);

        return prize;
    }

    private WheelPrize pickWeighted() {
        if (this.prizes.isEmpty() || this.totalWeight <= 0) return null;

        int roll = ThreadLocalRandom.current().nextInt(this.totalWeight);
        int acc = 0;
        for (WheelPrize prize : this.prizes) {
            acc += prize.weight;
            if (roll < acc) return prize;
        }
        return this.prizes.get(this.prizes.size() - 1);
    }

    private void giveReward(Habbo habbo, WheelPrize prize, WheelUserState state) {
        switch (prize.type) {
            case "credits":
                habbo.giveCredits(prize.amount);
                break;
            case "points":
                habbo.givePoints(prize.pointsType, prize.amount);
                break;
            case "spin":
                state.extraSpins += Math.max(0, prize.amount);
                break;
            case "item":
                this.giveItem(habbo, prize);
                break;
            case "badge":
                habbo.addBadge(prize.value, "Fortune Wheel");
                break;
            case "nothing":
            default:
                break;
        }
    }

    private void giveItem(Habbo habbo, WheelPrize prize) {
        int baseId;
        try {
            baseId = Integer.parseInt(prize.value.trim());
        } catch (NumberFormatException e) {
            return;
        }

        Item base = Emulator.getGameEnvironment().getItemManager().getItem(baseId);
        if (base == null) return;

        int quantity = Math.max(1, prize.amount);
        THashSet<HabboItem> items = new THashSet<>();
        for (int i = 0; i < quantity; i++) {
            HabboItem item = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), base, 0, 0, "");
            if (item != null) items.add(item);
        }

        if (!items.isEmpty()) {
            habbo.addFurniture(items);
        }
    }

    private void recordWin(Habbo habbo, WheelPrize prize) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO wheel_recent_wins (user_id, username, look, prize_label, won_at) VALUES (?, ?, ?, ?, ?)")) {
                statement.setInt(1, habbo.getHabboInfo().getId());
                statement.setString(2, habbo.getHabboInfo().getUsername());
                statement.setString(3, habbo.getHabboInfo().getLook());
                statement.setString(4, prize.label);
                statement.setInt(5, Emulator.getIntUnixTimestamp());
                statement.executeUpdate();
            }

            // Trim to the most recent RECENT_KEEP rows.
            try (PreparedStatement trim = connection.prepareStatement(
                    "DELETE FROM wheel_recent_wins WHERE id < (SELECT id FROM (SELECT id FROM wheel_recent_wins ORDER BY id DESC LIMIT 1 OFFSET ?) t)")) {
                trim.setInt(1, RECENT_KEEP - 1);
                trim.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to record wheel win", e);
        }
    }

    public List<WheelRecentWin> getRecentWins(int limit) {
        List<WheelRecentWin> wins = new ArrayList<>();
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT username, look, prize_label FROM wheel_recent_wins ORDER BY id DESC LIMIT ?")) {
            statement.setInt(1, limit);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    wins.add(new WheelRecentWin(set.getString("username"), set.getString("look"), set.getString("prize_label")));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load wheel recent wins", e);
        }
        return wins;
    }

    // Buys one extra spin with the configured currency. Returns false if the user can't afford it.
    public synchronized boolean buySpin(Habbo habbo) {
        if (this.spinCost <= 0) return false;

        if (this.spinCostType == -1) {
            if (habbo.getHabboInfo().getCredits() < this.spinCost) return false;
            habbo.giveCredits(-this.spinCost);
        } else {
            if (habbo.getHabboInfo().getCurrencyAmount(this.spinCostType) < this.spinCost) return false;
            habbo.givePoints(this.spinCostType, -this.spinCost);
        }

        int userId = habbo.getHabboInfo().getId();
        WheelUserState state = this.getUserState(userId);
        state.extraSpins++;
        this.persistUserState(userId, state);
        return true;
    }

    // Admin: update one prize row. Caller reloads once after a batch.
    public void savePrize(int id, String type, String value, int amount, int pointsType, int weight, String label) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE wheel_prizes SET type = ?, value = ?, amount = ?, points_type = ?, weight = ?, label = ? WHERE id = ?")) {
            statement.setString(1, type != null ? type : "nothing");
            statement.setString(2, value != null ? value : "");
            statement.setInt(3, amount);
            statement.setInt(4, pointsType);
            statement.setInt(5, Math.max(0, weight));
            statement.setString(6, label != null ? label : "");
            statement.setInt(7, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to save wheel prize {}", id, e);
        }
    }
}
