package pl.laina.homes.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import pl.laina.homes.LainaHomesPlugin;
import pl.laina.homes.message.Messages;
import pl.laina.homes.model.HomeEntry;
import pl.laina.homes.model.HomeSnapshot;
import pl.laina.homes.service.HomeGateway;
import pl.laina.homes.service.HomeTeleportCommand;
import pl.laina.homes.storage.GuiDataStore;
import pl.laina.homes.storage.PlayerGuiData;

public final class HomesMenuController {
    private final LainaHomesPlugin plugin;
    private final HomeGateway gateway;
    private final GuiDataStore dataStore;
    private final HomesMenuRenderer renderer;
    private final Messages messages;

    public HomesMenuController(LainaHomesPlugin plugin, HomeGateway gateway, GuiDataStore dataStore,
                               HomesMenuRenderer renderer, Messages messages) {
        this.plugin = plugin;
        this.gateway = gateway;
        this.dataStore = dataStore;
        this.renderer = renderer;
        this.messages = messages;
    }

    public void open(Player player) {
        this.open(player, 0);
    }

    public void open(Player player, int page) {
        try {
            List<HomeSnapshot> homes = this.gateway.homes(player);
            Set<String> existing = homes.stream().map(HomeSnapshot::name).collect(Collectors.toSet());
            PlayerGuiData data = this.dataStore.reconcile(player.getUniqueId(), existing);
            List<HomeEntry> entries = homes.stream()
                    .map(home -> new HomeEntry(home, data.isFavorite(home.name()), data.description(home.name())))
                    .toList();
            player.openInventory(this.renderer.render(entries, page, this.plugin.settings()));
        } catch (Exception exception) {
            this.plugin.getLogger().severe("Nie udało się otworzyć GUI home'ów gracza " + player.getName() + ": " + exception.getMessage());
            this.messages.send(player, "load-failed");
        }
    }

    public void teleport(Player player, String homeName) {
        if (this.gateway.find(player, homeName).isEmpty()) {
            player.closeInventory();
            this.messages.send(player, "home-missing");
            return;
        }
        final String command;
        try {
            command = HomeTeleportCommand.forHome(homeName);
        } catch (IllegalArgumentException exception) {
            player.closeInventory();
            this.messages.send(player, "unsafe-home-name");
            return;
        }
        player.closeInventory();
        player.performCommand(command);
    }

    public void toggleFavorite(Player player, String homeName, int page) {
        if (this.gateway.find(player, homeName).isEmpty()) {
            this.messages.send(player, "home-missing");
            this.open(player, page);
            return;
        }
        try {
            boolean added = this.dataStore.toggleFavorite(player.getUniqueId(), homeName);
            this.messages.send(player, added ? "favorite-added" : "favorite-removed", Map.of("home", homeName));
            this.open(player, page);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Nie udało się zapisać ulubionych gracza " + player.getName() + ": " + exception.getMessage());
            this.messages.send(player, "storage-failed");
        }
    }

    public boolean setDescription(Player player, String requestedHome, String description) {
        HomeSnapshot home = this.gateway.find(player, requestedHome).orElse(null);
        if (home == null) {
            this.messages.send(player, "home-missing");
            return false;
        }
        int max = this.plugin.settings().maxDescriptionLength();
        String stripped = description == null ? "" : description.strip();
        if (stripped.length() > max) {
            this.messages.send(player, "description-too-long", Map.of("max", Integer.toString(max)));
            return false;
        }
        try {
            this.dataStore.setDescription(player.getUniqueId(), home.name(), stripped);
            this.messages.send(player, stripped.isBlank() ? "description-cleared" : "description-saved", Map.of("home", home.name()));
            return true;
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Nie udało się zapisać opisu gracza " + player.getName() + ": " + exception.getMessage());
            this.messages.send(player, "storage-failed");
            return false;
        }
    }

    public List<String> homeNames(Player player) {
        return this.gateway.homes(player).stream().map(HomeSnapshot::name).toList();
    }

    public int homeCount(Player player) {
        return this.gateway.homes(player).size();
    }

    public String backendVersion() {
        return this.gateway.backendVersion();
    }
}
