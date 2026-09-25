package pl.laina.homes.gui;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.laina.homes.LainaHomesPlugin;
import pl.laina.homes.message.Messages;

public final class DescriptionEditor implements Listener {
    private static final String CANCEL_WORD = "anuluj";

    private final LainaHomesPlugin plugin;
    private final HomesMenuController menus;
    private final Messages messages;
    private final DescriptionEditSessions sessions = new DescriptionEditSessions(System::currentTimeMillis);

    public DescriptionEditor(LainaHomesPlugin plugin, HomesMenuController menus, Messages messages) {
        this.plugin = plugin;
        this.menus = menus;
        this.messages = messages;
    }

    public void begin(Player player, String homeName, int page) {
        if (!player.hasPermission("lainahomes.description")) {
            this.messages.send(player, "no-permission");
            return;
        }
        int timeoutSeconds = Math.max(10, Math.min(300,
                this.plugin.getConfig().getInt("descriptions.chat-input-timeout-seconds", 60)));
        DescriptionEditSession session = this.sessions.begin(
                player.getUniqueId(), homeName, page, timeoutSeconds * 1000L);
        player.closeInventory();
        this.messages.send(player, "description-edit-prompt", Map.of(
                "home", homeName,
                "max", Integer.toString(this.plugin.settings().maxDescriptionLength()),
                "seconds", Integer.toString(timeoutSeconds)
        ));
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (this.sessions.expire(player.getUniqueId(), session) && player.isOnline()) {
                this.messages.send(player, "description-edit-expired");
            }
        }, timeoutSeconds * 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        DescriptionEditSession session = this.sessions.consume(event.getPlayer().getUniqueId()).orElse(null);
        if (session == null) {
            return;
        }
        event.setCancelled(true);
        String input = PlainTextComponentSerializer.plainText().serialize(event.message()).strip();
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            if (input.toLowerCase(Locale.ROOT).equals(CANCEL_WORD)) {
                this.messages.send(event.getPlayer(), "description-edit-cancelled");
                this.menus.open(event.getPlayer(), session.page());
                return;
            }
            if (this.menus.setDescription(event.getPlayer(), session.homeName(), input.equals("-") ? "" : input)) {
                this.menus.open(event.getPlayer(), session.page());
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.sessions.cancel(event.getPlayer().getUniqueId());
    }
}
