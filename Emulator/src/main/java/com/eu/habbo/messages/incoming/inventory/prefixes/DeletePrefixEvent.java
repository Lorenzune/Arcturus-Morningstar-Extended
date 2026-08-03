package com.eu.habbo.messages.incoming.inventory.prefixes;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.UserPrefix;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.inventory.nickicons.UserNickIconsComposer;
import com.eu.habbo.messages.outgoing.inventory.prefixes.ActivePrefixUpdatedComposer;
import com.eu.habbo.messages.outgoing.inventory.prefixes.UserPrefixesComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;

public class DeletePrefixEvent extends MessageHandler {
    @Override
    public int getRatelimit() {
        return 500;
    }

    @Override
    public void handle() throws Exception {
        int prefixId = this.packet.readInt();

        UserPrefix prefix = this.client.getHabbo().getInventory().getPrefixesComponent().getPrefix(prefixId);

        if (prefix == null) return;

        boolean wasActive = prefix.isActive();

        this.client.getHabbo().getInventory().getPrefixesComponent().removePrefix(prefix);
        prefix.needsDelete(true);
        Emulator.getThreading().run(prefix);

        this.client.sendResponse(new UserPrefixesComposer(this.client.getHabbo()));

        if (wasActive) {
            this.client.sendResponse(new ActivePrefixUpdatedComposer(null));
            this.client.sendResponse(new UserNickIconsComposer(this.client.getHabbo()));

            if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
                this.client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(this.client.getHabbo()).compose());
            }
        }
    }
}
