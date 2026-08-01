package com.eu.habbo.messages.incoming.rooms.bots;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.bots.BotManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class BotPlaceEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();

        if (room == null)
            return;

        int botId = Math.abs(this.packet.readInt());

        int x = this.packet.readInt();
        int y = this.packet.readInt();
        Bot bot = this.client.getHabbo().getInventory().getBotsComponent().getBot(botId);
        BotManager botManager = Emulator.getGameEnvironment().getBotManager();

        if (bot != null) {
            botManager.placeBot(bot, this.client.getHabbo(), room, room.getLayout().getTile((short) x, (short) y));
            return;
        }

        bot = room.getBot(botId);

        if (bot == null)
            return;

        botManager.moveBot(bot, this.client.getHabbo(), room, room.getLayout().getTile((short) x, (short) y));
    }
}
