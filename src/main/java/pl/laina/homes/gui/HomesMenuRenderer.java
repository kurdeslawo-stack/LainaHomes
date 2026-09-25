package pl.laina.homes.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.laina.homes.config.ConfiguredIcon;
import pl.laina.homes.config.GuiIcon;
import pl.laina.homes.config.GuiSettings;
import pl.laina.homes.core.HomeCatalogue;
import pl.laina.homes.core.Page;
import pl.laina.homes.message.Messages;
import pl.laina.homes.model.HomeEntry;

public final class HomesMenuRenderer {
    private final Messages messages;
    private final HomeCatalogue catalogue;

    public HomesMenuRenderer(Messages messages, HomeCatalogue catalogue) {
        this.messages = messages;
        this.catalogue = catalogue;
    }

    public Inventory render(List<HomeEntry> homes, int requestedPage, GuiSettings settings) {
        Page<HomeEntry> page = this.catalogue.page(homes, requestedPage, settings.homeSlots().size());
        Map<String, String> pagePlaceholders = Map.of(
                "page", Integer.toString(page.index() + 1),
                "pages", Integer.toString(page.totalPages())
        );
        HomesHolder holder = new HomesHolder(page.index());
        Inventory inventory = Bukkit.createInventory(holder, settings.size(), this.messages.parse(settings.title(), pagePlaceholders));
        holder.attach(inventory);
        this.fill(inventory, settings.icon(GuiIcon.FILLER));

        for (int index = 0; index < page.entries().size(); index++) {
            int slot = settings.homeSlots().get(index);
            HomeEntry home = page.entries().get(index);
            holder.bindHome(slot, home);
            inventory.setItem(slot, this.homeItem(home, settings));
        }

        if (page.totalEntries() == 0) {
            int slot = settings.homeSlots().get(settings.homeSlots().size() / 2);
            inventory.setItem(slot, this.item(settings.icon(GuiIcon.EMPTY_STATE), settings.emptyName(), settings.emptyLore()));
        }
        if (page.hasPrevious()) {
            holder.bindAction(settings.previousSlot(), MenuAction.PREVIOUS);
            inventory.setItem(settings.previousSlot(), this.item(settings.icon(GuiIcon.PREVIOUS_PAGE), settings.previousName(), List.of()));
        }
        if (page.hasNext()) {
            holder.bindAction(settings.nextSlot(), MenuAction.NEXT);
            inventory.setItem(settings.nextSlot(), this.item(settings.icon(GuiIcon.NEXT_PAGE), settings.nextName(), List.of()));
        }
        holder.bindAction(settings.refreshSlot(), MenuAction.REFRESH);
        inventory.setItem(settings.refreshSlot(), this.item(settings.icon(GuiIcon.REFRESH), settings.refreshName(), List.of()));

        long favorites = homes.stream().filter(HomeEntry::favorite).count();
        Map<String, String> summary = Map.of(
                "total", Integer.toString(homes.size()),
                "favorites", Long.toString(favorites)
        );
        inventory.setItem(settings.summarySlot(), this.item(
                settings.icon(GuiIcon.SUMMARY),
                replace(settings.summaryName(), summary),
                settings.summaryLore().stream().map(line -> replace(line, summary)).toList()
        ));
        return inventory;
    }

    private ItemStack homeItem(HomeEntry entry, GuiSettings settings) {
        DecimalFormat decimal = settings.decimalFormat();
        String coordinates = settings.coordinatesFormat()
                .replace("%x%", decimal.format(entry.home().x()))
                .replace("%y%", decimal.format(entry.home().y()))
                .replace("%z%", decimal.format(entry.home().z()));
        HashMap<String, String> plain = new HashMap<>();
        plain.put("home", entry.home().name());
        plain.put("world", entry.home().world());
        plain.put("x", decimal.format(entry.home().x()));
        plain.put("y", decimal.format(entry.home().y()));
        plain.put("z", decimal.format(entry.home().z()));
        plain.put("coordinates", coordinates);

        String name = replaceEscaped(settings.homeName(), plain);
        ArrayList<String> lore = new ArrayList<>();
        for (String line : settings.homeLore()) {
            if (settings.hideEmptyDescriptionLine() && entry.description().isBlank() && line.contains("%description%")) {
                continue;
            }
            String rendered = replaceEscaped(line, plain)
                    .replace("%description%", entry.description().isBlank()
                            ? settings.noDescription()
                            : this.messages.escape(entry.description()))
                    .replace("%favorite%", entry.favorite() ? settings.favoriteYes() : settings.favoriteNo());
            lore.add(rendered);
        }
        return this.item(settings.icon(entry.favorite() ? GuiIcon.FAVORITE_HOME : GuiIcon.HOME), name, lore);
    }

    private ItemStack item(ConfiguredIcon icon, String name, List<String> lore) {
        ItemStack item = new ItemStack(icon.material());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(this.messages.parse("<italic:false>" + name));
        meta.lore(lore.stream().map(line -> this.messages.parse("<italic:false>" + line)).toList());
        icon.applyTo(meta);
        item.setItemMeta(meta);
        return item;
    }

    private void fill(Inventory inventory, ConfiguredIcon icon) {
        ItemStack filler = this.item(icon, " ", List.of());
        ItemMeta meta = filler.getItemMeta();
        meta.setHideTooltip(true);
        filler.setItemMeta(meta);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private String replaceEscaped(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", this.messages.escape(entry.getValue()));
        }
        return result;
    }

    private static String replace(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }
}
