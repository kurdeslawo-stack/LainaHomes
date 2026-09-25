package pl.laina.homes.storage;

import java.util.Map;
import java.util.Set;

public record PlayerGuiData(Set<String> favorites, Map<String, String> descriptions) {
    public PlayerGuiData {
        favorites = Set.copyOf(favorites);
        descriptions = Map.copyOf(descriptions);
    }

    public boolean isFavorite(String homeName) {
        return this.favorites.contains(GuiDataStore.normalize(homeName));
    }

    public String description(String homeName) {
        return this.descriptions.getOrDefault(GuiDataStore.normalize(homeName), "");
    }
}
