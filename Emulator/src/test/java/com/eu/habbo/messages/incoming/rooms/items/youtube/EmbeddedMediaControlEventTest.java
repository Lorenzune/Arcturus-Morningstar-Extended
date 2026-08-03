package com.eu.habbo.messages.incoming.rooms.items.youtube;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmbeddedMediaControlEventTest {
    @Test
    void acceptsHttpAndHttpsEmbedUrls() {
        assertEquals(
                "https://streamingcommunityz.support/it/watch/65277",
                EmbeddedMediaControlEvent.validateUrl("https://streamingcommunityz.support/it/watch/65277")
        );
        assertEquals("http://example.com/video.mp4", EmbeddedMediaControlEvent.validateUrl("http://example.com/video.mp4"));
    }

    @Test
    void rejectsUnsafeOrNonAbsoluteUrls() {
        assertNull(EmbeddedMediaControlEvent.validateUrl("javascript:alert(1)"));
        assertNull(EmbeddedMediaControlEvent.validateUrl("data:text/html,test"));
        assertNull(EmbeddedMediaControlEvent.validateUrl("/relative/path"));
        assertNull(EmbeddedMediaControlEvent.validateUrl("https://user:password@example.com/private"));
    }
}
