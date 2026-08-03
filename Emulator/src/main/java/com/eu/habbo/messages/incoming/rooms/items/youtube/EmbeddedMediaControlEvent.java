package com.eu.habbo.messages.incoming.rooms.items.youtube;

import com.eu.habbo.habbohotel.items.interactions.InteractionYoutubeTV;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.incoming.rooms.items.RoomItemInputGuard;
import com.eu.habbo.messages.outgoing.rooms.youtube.EmbeddedMediaStateComposer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;

public class EmbeddedMediaControlEvent extends MessageHandler {
    private static final int MAX_URL_LENGTH = 2048;
    private static final int MAX_SIGNAL_LENGTH = 65535;

    @Override
    public int getRatelimit() { return 0; }

    @Override
    public void handle() throws Exception {
        int itemId = this.packet.readInt();
        int action = this.packet.readInt();
        String requestedData = RoomItemInputGuard.trimToMax(this.packet.readString(), MAX_SIGNAL_LENGTH);

        if (!RoomItemInputGuard.isPositiveId(itemId) || action < 0 || action > 8) return;

        Habbo habbo = this.client.getHabbo();
        if (habbo == null) return;

        Room room = habbo.getHabboInfo().getCurrentRoom();
        if (room == null) return;

        HabboItem roomItem = room.getHabboItem(itemId);
        if (!(roomItem instanceof InteractionYoutubeTV)) return;

        InteractionYoutubeTV item = (InteractionYoutubeTV) roomItem;
        if (!item.isEmbeddedPlayer()) return;

        if (action == 8) {
            relaySignal(room, item, habbo, requestedData);
            return;
        }

        if (action == 0) {
            if (item.getEmbeddedState() == 3 && room.getHabbo(item.getEmbeddedBroadcasterId()) == null) {
                item.stopEmbedded();
                room.sendComposer(new EmbeddedMediaStateComposer(item, 7).compose());
                return;
            }
            this.client.sendResponse(new EmbeddedMediaStateComposer(item, 0));
            return;
        }

        if (action == 7 && item.getEmbeddedState() == 3 && room.getHabbo(item.getEmbeddedBroadcasterId()) == null) {
            item.stopEmbedded();
            room.sendComposer(new EmbeddedMediaStateComposer(item, 7).compose());
            return;
        }

        boolean canControl = item.getUserId() == habbo.getHabboInfo().getId()
                || room.isOwner(habbo)
                || habbo.hasPermission(Permission.ACC_ANYROOMOWNER);
        if (!canControl) return;

        switch (action) {
            case 1:
                String safeUrl = validateUrl(requestedData);
                if (safeUrl == null) return;
                item.setEmbeddedUrl(safeUrl);
                item.needsUpdate(true);
                room.updateItem(item);
                break;
            case 2:
                item.playEmbedded();
                break;
            case 3:
                item.pauseEmbedded();
                break;
            case 4:
                item.reloadEmbedded();
                break;
            case 5:
                item.stopEmbedded();
                break;
            case 6:
                item.startEmbeddedShare(habbo.getHabboInfo().getId());
                break;
            case 7:
                if (item.getEmbeddedState() != 3) return;
                item.stopEmbedded();
                break;
            default:
                return;
        }

        room.sendComposer(new EmbeddedMediaStateComposer(item, action).compose());
    }

    private static void relaySignal(Room room, InteractionYoutubeTV item, Habbo sender, String requestedData) {
        if (item.getEmbeddedState() != 3 || item.getEmbeddedBroadcasterId() <= 0 || requestedData.isBlank()) return;

        try {
            JsonObject signal = JsonParser.parseString(requestedData).getAsJsonObject();
            int targetUserId = signal.get("targetUserId").getAsInt();
            String type = signal.get("type").getAsString();
            int senderUserId = sender.getHabboInfo().getId();
            int broadcasterId = item.getEmbeddedBroadcasterId();

            if (!RoomItemInputGuard.isPositiveId(targetUserId)
                    || targetUserId == senderUserId
                    || type.length() > 16
                    || !(type.equals("ready") || type.equals("offer") || type.equals("answer") || type.equals("ice"))) return;

            boolean validPair = (type.equals("offer") && senderUserId == broadcasterId)
                    || ((type.equals("ready") || type.equals("answer")) && targetUserId == broadcasterId)
                    || (type.equals("ice") && (senderUserId == broadcasterId || targetUserId == broadcasterId));
            if (!validPair) return;

            Habbo target = room.getHabbo(targetUserId);
            if (target == null || target.getClient() == null) return;

            target.getClient().sendResponse(new EmbeddedMediaStateComposer(item, requestedData, senderUserId));
        } catch (RuntimeException ignored) {
            // Malformed or incomplete WebRTC signaling payloads are ignored.
        }
    }

    static String validateUrl(String input) {
        if (input == null || input.isBlank() || input.length() > MAX_URL_LENGTH) return null;

        try {
            URI uri = URI.create(input.trim());
            String scheme = uri.getScheme();
            if (scheme == null || uri.getHost() == null) return null;
            if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) return null;
            if (uri.getUserInfo() != null) return null;
            return uri.toASCIIString();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
