package pl.laina.homes.gui;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

final class DescriptionEditSessions {
    private final ConcurrentHashMap<UUID, DescriptionEditSession> sessions = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    DescriptionEditSessions(LongSupplier clock) {
        this.clock = clock;
    }

    DescriptionEditSession begin(UUID playerId, String homeName, int page, long timeoutMillis) {
        DescriptionEditSession session = new DescriptionEditSession(
                homeName,
                page,
                this.clock.getAsLong() + Math.max(1L, timeoutMillis)
        );
        this.sessions.put(playerId, session);
        return session;
    }

    Optional<DescriptionEditSession> consume(UUID playerId) {
        DescriptionEditSession session = this.sessions.remove(playerId);
        if (session == null || this.clock.getAsLong() > session.expiresAtMillis()) {
            return Optional.empty();
        }
        return Optional.of(session);
    }

    boolean expire(UUID playerId, DescriptionEditSession expected) {
        return this.sessions.remove(playerId, expected);
    }

    void cancel(UUID playerId) {
        this.sessions.remove(playerId);
    }
}
