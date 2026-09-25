package pl.laina.homes.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class GuiListener implements Listener {
    private final HomesMenuController menus;
    private final java.util.function.LongSupplier spamDelayMillis;
    private final Map<UUID, Long> nextClickAt = new HashMap<>();

    public GuiListener(HomesMenuController menus, java.util.function.LongSupplier spamDelayMillis) {
        this.menus = menus;
        this.spamDelayMillis = spamDelayMillis;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof HomesHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() != event.getView().getTopInventory() || event.getRawSlot() < 0
                || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }
        ClickType click = event.getClick();
        if (click != ClickType.LEFT && click != ClickType.RIGHT) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.nextClickAt.getOrDefault(player.getUniqueId(), 0L)) {
            return;
        }
        this.nextClickAt.put(player.getUniqueId(), now + this.spamDelayMillis.getAsLong());

        var home = holder.homeAt(event.getRawSlot());
        if (home.isPresent()) {
            if (click == ClickType.LEFT) {
                this.menus.teleport(player, home.get().home().name());
            } else {
                this.menus.toggleFavorite(player, home.get().home().name(), holder.page());
            }
            return;
        }
        if (click != ClickType.LEFT) {
            return;
        }
        holder.actionAt(event.getRawSlot()).ifPresent(action -> {
            switch (action) {
                case PREVIOUS -> this.menus.open(player, holder.page() - 1);
                case NEXT -> this.menus.open(player, holder.page() + 1);
                case REFRESH -> this.menus.open(player, holder.page());
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof HomesHolder) {
            event.setCancelled(true);
        }
    }
}
