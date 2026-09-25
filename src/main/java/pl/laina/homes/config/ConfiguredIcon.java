package pl.laina.homes.config;

import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public record ConfiguredIcon(Material material, int customModelData) {
    public ConfiguredIcon {
        Objects.requireNonNull(material, "material");
        customModelData = Math.max(0, customModelData);
    }

    public void applyTo(ItemMeta meta) {
        if (this.customModelData == 0) {
            return;
        }
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setFloats(List.of((float) this.customModelData));
        meta.setCustomModelDataComponent(component);
    }
}
