package com.eu.habbo.messages.incoming.inventory.nickicons;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.UserNickIcon;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.nickicons.UserNickIconsComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;

public class DeleteNickIconEvent extends MessageHandler {
    @Override
    public int getRatelimit() {
        return 500;
    }

    @Override
    public void handle() throws Exception {
        int nickIconId = this.packet.readInt();

        UserNickIcon nickIcon = this.client.getHabbo().getInventory().getNickIconsComponent().getNickIcon(nickIconId);

        if (nickIcon == null) return;

        boolean wasActive = nickIcon.isActive();

        this.client.getHabbo().getInventory().getNickIconsComponent().removeNickIcon(nickIcon);
        nickIcon.needsDelete(true);
        Emulator.getThreading().run(nickIcon);

        this.client.sendResponse(new UserNickIconsComposer(this.client.getHabbo()));

        if (wasActive && this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(this.client.getHabbo()).compose());
        }
    }
}
