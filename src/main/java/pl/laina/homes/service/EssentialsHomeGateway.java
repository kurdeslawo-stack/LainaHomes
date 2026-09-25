package pl.laina.homes.service;

import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.ess3.api.IEssentials;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pl.laina.homes.model.HomeSnapshot;

public final class EssentialsHomeGateway implements HomeGateway {
    private final IEssentials essentials;
    private final String version;
    private final Consumer<String> warningLogger;

    public EssentialsHomeGateway(IEssentials essentials, String version, Consumer<String> warningLogger) {
        this.essentials = Objects.requireNonNull(essentials, "essentials");
        this.version = Objects.requireNonNull(version, "version");
        this.warningLogger = Objects.requireNonNull(warningLogger, "warningLogger");
    }

    @Override
    public List<HomeSnapshot> homes(Player player) {
        User user = this.essentials.getUser(player);
        if (user == null) {
            return List.of();
        }
        ArrayList<HomeSnapshot> homes = new ArrayList<>();
        for (String name : List.copyOf(user.getHomes())) {
            try {
                Location location = user.getHome(name);
                if (location == null) {
                    continue;
                }
                World world = location.getWorld();
                homes.add(new HomeSnapshot(
                        name,
                        world == null ? "nieznany" : world.getName(),
                        location.getX(),
                        location.getY(),
                        location.getZ()
                ));
            } catch (Exception exception) {
                this.warningLogger.accept("Nie udało się odczytać home'a '" + name + "' gracza " + player.getName() + ": " + exception.getMessage());
            }
        }
        return List.copyOf(homes);
    }

    @Override
    public String backendVersion() {
        return "EssentialsX " + this.version;
    }
}
