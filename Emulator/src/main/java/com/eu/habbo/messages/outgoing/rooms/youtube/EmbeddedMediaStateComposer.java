package com.eu.habbo.messages.outgoing.rooms.youtube;

import com.eu.habbo.habbohotel.items.interactions.InteractionYoutubeTV;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

public class EmbeddedMediaStateComposer extends MessageComposer {
    private final InteractionYoutubeTV item;
    private final int action;
    private final String signalPayload;
    private final int signalSenderId;

    public EmbeddedMediaStateComposer(InteractionYoutubeTV item, int action) {
        this.item = item;
        this.action = action;
        this.signalPayload = null;
        this.signalSenderId = 0;
    }

    public EmbeddedMediaStateComposer(InteractionYoutubeTV item, String signalPayload, int signalSenderId) {
        this.item = item;
        this.action = 8;
        this.signalPayload = signalPayload;
        this.signalSenderId = signalSenderId;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.EmbeddedMediaStateComposer);
        this.response.appendInt(this.item.getId());
        this.response.appendInt(this.action);
        this.response.appendString(this.signalPayload == null ? this.item.getEmbeddedUrl() : this.signalPayload);
        this.response.appendInt(this.signalPayload == null ? this.item.getEmbeddedState() : 0);
        this.response.appendInt(this.signalPayload == null ? this.item.getEmbeddedStartedAt() : this.signalSenderId);
        this.response.appendInt(this.item.getEmbeddedOffset());
        this.response.appendInt(this.item.getEmbeddedRevision());
        this.response.appendInt(this.item.getEmbeddedBroadcasterId());
        return this.response;
    }
}
