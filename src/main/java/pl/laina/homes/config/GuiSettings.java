package pl.laina.homes.config;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public record GuiSettings(
        String title,
        int size,
        List<Integer> homeSlots,
        int previousSlot,
        int summarySlot,
        int refreshSlot,
        int nextSlot,
        Map<GuiIcon, ConfiguredIcon> icons,
        String homeName,
        List<String> homeLore,
        String descriptionEditHint,
        String previousName,
        String nextName,
        String refreshName,
        String summaryName,
        List<String> summaryLore,
        String emptyName,
        List<String> emptyLore,
        String coordinatesFormat,
        String numberFormat,
        String favoriteYes,
        String favoriteNo,
        String noDescription,
        boolean hideEmptyDescriptionLine,
        long spamDelayMillis,
        int maxDescriptionLength
) {
    private static final List<Integer> DEFAULT_HOME_SLOTS = List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );

    public GuiSettings {
        homeSlots = List.copyOf(homeSlots);
        icons = Map.copyOf(icons);
        homeLore = List.copyOf(homeLore);
        summaryLore = List.copyOf(summaryLore);
        emptyLore = List.copyOf(emptyLore);
    }

    public static GuiSettings load(FileConfiguration config, Consumer<String> warningLogger) {
        return load(config, warningLogger, Material::isItem);
    }

    static GuiSettings load(FileConfiguration config, Consumer<String> warningLogger,
                            Predicate<Material> itemValidator) {
        int requestedSize = config.getInt("gui.size", 54);
        int size = requestedSize >= 9 && requestedSize <= 54 && requestedSize % 9 == 0 ? requestedSize : 54;
        if (size != requestedSize) {
            warningLogger.accept("Nieprawidłowe gui.size: " + requestedSize + ". Używam 54.");
        }
        int previous = slot(config, "gui.slots.previous-page", 45, size, warningLogger);
        int summary = slot(config, "gui.slots.summary", 49, size, warningLogger);
        int refresh = slot(config, "gui.slots.refresh", 51, size, warningLogger);
        int next = slot(config, "gui.slots.next-page", 53, size, warningLogger);
        Set<Integer> reserved = new java.util.HashSet<>(List.of(previous, summary, refresh, next));
        LinkedHashSet<Integer> configuredSlots = new LinkedHashSet<>();
        for (Integer value : config.getIntegerList("gui.home-slots")) {
            if (value != null && value >= 0 && value < size && !reserved.contains(value)) {
                configuredSlots.add(value);
            }
        }
        if (configuredSlots.isEmpty()) {
            DEFAULT_HOME_SLOTS.stream().filter(slot -> slot < size && !reserved.contains(slot)).forEach(configuredSlots::add);
        }
        if (configuredSlots.isEmpty()) {
            throw new IllegalArgumentException("GUI nie ma ani jednego poprawnego slotu na home'y.");
        }

        EnumMap<GuiIcon, ConfiguredIcon> icons = new EnumMap<>(GuiIcon.class);
        ConfigurationSection iconSection = config.getConfigurationSection("gui.icons");
        for (GuiIcon icon : GuiIcon.values()) {
            Material material = icon.fallback();
            int cmd = 0;
            if (iconSection != null) {
                String raw = iconSection.getString(icon.configKey() + ".material");
                if (raw != null && !raw.isBlank()) {
                    Material candidate = Material.matchMaterial(raw);
                    if (candidate == null || isAir(candidate) || !itemValidator.test(candidate)) {
                        warningLogger.accept("Nieprawidłowy material gui.icons." + icon.configKey() + ".material: '" + raw + "'. Używam " + icon.fallback() + ".");
                    } else {
                        material = candidate;
                    }
                }
                cmd = Math.max(0, iconSection.getInt(icon.configKey() + ".custom-model-data", 0));
            }
            icons.put(icon, new ConfiguredIcon(material, cmd));
        }

        return new GuiSettings(
                config.getString("gui.title", "<green><bold>Twoje home'y</bold>"),
                size,
                new ArrayList<>(configuredSlots),
                previous,
                summary,
                refresh,
                next,
                icons,
                config.getString("gui.text.home-name", "<green><bold>%home%</bold>"),
                config.getStringList("gui.text.home-lore"),
                config.getString("gui.text.description-edit-hint", "<aqua>✎ Shift + PPM: ustaw/zmień opis"),
                config.getString("gui.text.previous-page", "<green>Poprzednia strona"),
                config.getString("gui.text.next-page", "<green>Następna strona"),
                config.getString("gui.text.refresh", "<yellow>Odśwież listę"),
                config.getString("gui.text.summary", "<gold><bold>Podsumowanie</bold>"),
                config.getStringList("gui.text.summary-lore"),
                config.getString("gui.text.empty-state", "<yellow><bold>Brak home'ów</bold>"),
                config.getStringList("gui.text.empty-lore"),
                config.getString("gui.formats.coordinates", "%x%, %y%, %z%"),
                validNumberFormat(config.getString("gui.formats.number", "0.0"), warningLogger),
                config.getString("gui.formats.favorite-yes", "<gold>★ tak"),
                config.getString("gui.formats.favorite-no", "<dark_gray>☆ nie"),
                config.getString("gui.formats.no-description", "<dark_gray>brak"),
                config.getBoolean("gui.formats.hide-empty-description-line", false),
                Math.max(0L, config.getLong("gui.spam-click-delay-ms", 250L)),
                Math.max(1, Math.min(1000, config.getInt("descriptions.max-length", 120)))
        );
    }

    private static boolean isAir(Material material) {
        return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
    }

    public ConfiguredIcon icon(GuiIcon icon) {
        return this.icons.get(icon);
    }

    public DecimalFormat decimalFormat() {
        return new DecimalFormat(this.numberFormat, DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    private static int slot(FileConfiguration config, String path, int fallback, int size, Consumer<String> warnings) {
        int value = config.getInt(path, fallback);
        if (value < 0 || value >= size) {
            int safeFallback = Math.min(fallback, size - 1);
            warnings.accept("Nieprawidłowy slot " + path + ": " + value + ". Używam " + safeFallback + ".");
            return safeFallback;
        }
        return value;
    }

    private static String validNumberFormat(String raw, Consumer<String> warnings) {
        try {
            new DecimalFormat(raw, DecimalFormatSymbols.getInstance(Locale.ROOT));
            return raw;
        } catch (IllegalArgumentException exception) {
            warnings.accept("Nieprawidłowy gui.formats.number: '" + raw + "'. Używam 0.0.");
            return "0.0";
        }
    }
}
