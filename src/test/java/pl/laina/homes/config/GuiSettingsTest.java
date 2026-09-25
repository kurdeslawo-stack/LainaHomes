package pl.laina.homes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class GuiSettingsTest {
    @Test
    void loadsCustomLayoutMaterialAndCustomModelData() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
                gui:
                  size: 27
                  home-slots: [10, 11, 26, 99]
                  slots:
                    previous-page: 18
                    summary: 22
                    refresh: 24
                    next-page: 26
                  icons:
                    home:
                      material: ECHO_SHARD
                      custom-model-data: 2351601
                descriptions:
                  max-length: 80
                """);

        GuiSettings settings = GuiSettings.load(config, message -> {}, material -> true);

        assertEquals(27, settings.size());
        assertEquals(java.util.List.of(10, 11), settings.homeSlots());
        assertEquals(Material.ECHO_SHARD, settings.icon(GuiIcon.HOME).material());
        assertEquals(2351601, settings.icon(GuiIcon.HOME).customModelData());
        assertEquals(80, settings.maxDescriptionLength());
    }

    @Test
    void invalidValuesUseSafeFallbacks() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
                gui:
                  size: 13
                  home-slots: []
                  icons:
                    home:
                      material: AIR
                """);
        ArrayList<String> warnings = new ArrayList<>();

        GuiSettings settings = GuiSettings.load(config, warnings::add, material -> true);

        assertEquals(54, settings.size());
        assertEquals(Material.GRASS_BLOCK, settings.icon(GuiIcon.HOME).material());
        assertFalse(settings.homeSlots().isEmpty());
        assertTrue(warnings.size() >= 2);
    }
}
