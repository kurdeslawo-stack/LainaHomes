package pl.laina.homes.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import pl.laina.homes.model.HomeEntry;

public final class HomesHolder implements InventoryHolder {
    private final int page;
    private final Map<Integer, HomeEntry> homes = new HashMap<>();
    private final Map<Integer, MenuAction> actions = new HashMap<>();
    private Inventory inventory;

    public HomesHolder(int page) {
        this.page = page;
    }

    public int page() {
        return this.page;
    }

    public void bindHome(int slot, HomeEntry home) {
        this.homes.put(slot, home);
    }

    public void bindAction(int slot, MenuAction action) {
        this.actions.put(slot, action);
    }

    public Optional<HomeEntry> homeAt(int slot) {
        return Optional.ofNullable(this.homes.get(slot));
    }

    public Optional<MenuAction> actionAt(int slot) {
        return Optional.ofNullable(this.actions.get(slot));
    }

    public void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
