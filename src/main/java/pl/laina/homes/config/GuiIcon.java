package pl.laina.homes.config;

import org.bukkit.Material;

public enum GuiIcon {
    FILLER("filler", Material.BLACK_STAINED_GLASS_PANE),
    HOME("home", Material.GRASS_BLOCK),
    FAVORITE_HOME("favorite-home", Material.GOLD_BLOCK),
    PREVIOUS_PAGE("previous-page", Material.ARROW),
    NEXT_PAGE("next-page", Material.ARROW),
    REFRESH("refresh", Material.SUNFLOWER),
    SUMMARY("summary", Material.BOOK),
    EMPTY_STATE("empty-state", Material.FLOWER_POT);

    private final String configKey;
    private final Material fallback;

    GuiIcon(String configKey, Material fallback) {
        this.configKey = configKey;
        this.fallback = fallback;
    }

    public String configKey() {
        return this.configKey;
    }

    public Material fallback() {
        return this.fallback;
    }
}
