package pl.laina.homes.service;

import java.util.List;
import java.util.Optional;
import org.bukkit.entity.Player;
import pl.laina.homes.model.HomeSnapshot;

public interface HomeGateway {
    List<HomeSnapshot> homes(Player player);

    default Optional<HomeSnapshot> find(Player player, String name) {
        return this.homes(player).stream().filter(home -> home.name().equalsIgnoreCase(name)).findFirst();
    }

    String backendVersion();
}
