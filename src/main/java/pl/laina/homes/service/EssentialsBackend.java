package pl.laina.homes.service;

import java.util.Optional;
import net.ess3.api.IEssentials;
import org.bukkit.plugin.Plugin;

public final class EssentialsBackend {
    private EssentialsBackend() {
    }

    public static Optional<IEssentials> resolve(Plugin plugin) {
        if (plugin instanceof IEssentials essentials && plugin.isEnabled()) {
            return Optional.of(essentials);
        }
        return Optional.empty();
    }
}
