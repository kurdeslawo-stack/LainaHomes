package pl.laina.homes.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class DescriptionEditSessionsTest {
    @Test
    void consumesActiveSessionOnlyOnce() {
        AtomicLong clock = new AtomicLong(1000L);
        DescriptionEditSessions sessions = new DescriptionEditSessions(clock::get);
        UUID player = UUID.randomUUID();
        sessions.begin(player, "baza", 2, 60_000L);

        DescriptionEditSession session = sessions.consume(player).orElseThrow();

        assertEquals("baza", session.homeName());
        assertEquals(2, session.page());
        assertTrue(sessions.consume(player).isEmpty());
    }

    @Test
    void ignoresExpiredSessionAndDoesNotExpireReplacement() {
        AtomicLong clock = new AtomicLong(1000L);
        DescriptionEditSessions sessions = new DescriptionEditSessions(clock::get);
        UUID player = UUID.randomUUID();
        DescriptionEditSession old = sessions.begin(player, "stary", 0, 100L);
        clock.set(1200L);
        assertTrue(sessions.consume(player).isEmpty());

        DescriptionEditSession current = sessions.begin(player, "nowy", 1, 1000L);
        assertFalse(sessions.expire(player, old));
        assertEquals(current, sessions.consume(player).orElseThrow());
    }
}
