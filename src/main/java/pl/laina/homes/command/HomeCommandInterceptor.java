package pl.laina.homes.command;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pl.laina.homes.gui.HomesMenuController;
import pl.laina.homes.message.Messages;

public final class HomeCommandInterceptor implements Listener {
    private final HomesMenuController menus;
    private final Messages messages;

    public HomeCommandInterceptor(HomesMenuController menus, Messages messages) {
        this.menus = menus;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!HomeCommandRoute.opensGui(event.getMessage())) {
            return;
        }
        event.setCancelled(true);
        if (!event.getPlayer().hasPermission("lainahomes.use")) {
            this.messages.send(event.getPlayer(), "no-permission");
            return;
        }
        this.menus.open(event.getPlayer());
    }
}
