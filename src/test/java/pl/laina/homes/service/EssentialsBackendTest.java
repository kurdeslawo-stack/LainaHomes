package pl.laina.homes.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.ess3.api.IEssentials;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

class EssentialsBackendTest {
    @Test
    void rejectsMissingWrongOrDisabledBackend() {
        assertTrue(EssentialsBackend.resolve(null).isEmpty());
        assertTrue(EssentialsBackend.resolve(mock(Plugin.class)).isEmpty());

        IEssentials disabled = mock(IEssentials.class);
        when(disabled.isEnabled()).thenReturn(false);
        assertTrue(EssentialsBackend.resolve(disabled).isEmpty());
    }

    @Test
    void acceptsEnabledEssentialsBackend() {
        IEssentials essentials = mock(IEssentials.class);
        when(essentials.isEnabled()).thenReturn(true);

        assertSame(essentials, EssentialsBackend.resolve(essentials).orElseThrow());
    }
}
