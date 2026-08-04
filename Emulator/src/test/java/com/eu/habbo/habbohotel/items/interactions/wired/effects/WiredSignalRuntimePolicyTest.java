package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class WiredSignalRuntimePolicyTest {

    @Test
    void sendSignalCanReenterWithoutCooldown() {
        WiredEffectSendSignal effect = mock(WiredEffectSendSignal.class, CALLS_REAL_METHODS);

        assertEquals(0L, effect.requiredCooldown());
    }

    @Test
    void zeroSignalDepthLeavesFeedbackChainsUnlimited() {
        int previousLimit = WiredEffectSendSignal.MAX_SIGNAL_DEPTH;
        try {
            WiredEffectSendSignal.MAX_SIGNAL_DEPTH = 0;
            assertFalse(WiredEffectSendSignal.hasReachedMaxSignalDepth(Integer.MAX_VALUE));

            WiredEffectSendSignal.MAX_SIGNAL_DEPTH = 100;
            assertFalse(WiredEffectSendSignal.hasReachedMaxSignalDepth(99));
            assertTrue(WiredEffectSendSignal.hasReachedMaxSignalDepth(100));
        } finally {
            WiredEffectSendSignal.MAX_SIGNAL_DEPTH = previousLimit;
        }
    }
}
