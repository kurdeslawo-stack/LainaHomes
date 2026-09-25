package pl.laina.homes.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class GuiDataStore {
    private static final String PLAYERS = "players";

    private final Path file;
    private final Consumer<String> warningLogger;
    private final Map<UUID, MutableData> players = new HashMap<>();

    public GuiDataStore(Path file, Consumer<String> warningLogger) {
        this.file = file;
        this.warningLogger = warningLogger;
        this.load();
    }

    public synchronized PlayerGuiData reconcile(UUID playerId, Set<String> existingHomes) throws IOException {
        Set<String> existing = existingHomes.stream().map(GuiDataStore::normalize).collect(java.util.stream.Collectors.toSet());
        MutableData data = this.players.get(playerId);
        if (data == null) {
            return new PlayerGuiData(Set.of(), Map.of());
        }
        boolean changed = data.favorites.removeIf(home -> !existing.contains(home));
        changed |= data.descriptions.keySet().removeIf(home -> !existing.contains(home));
        if (data.isEmpty()) {
            this.players.remove(playerId);
            changed = true;
        }
        if (changed) {
            this.save();
        }
        return this.snapshot(playerId);
    }

    public synchronized boolean toggleFavorite(UUID playerId, String homeName) throws IOException {
        String home = normalize(homeName);
        MutableData data = this.players.computeIfAbsent(playerId, ignored -> new MutableData());
        boolean added = data.favorites.add(home);
        if (!added) {
            data.favorites.remove(home);
        }
        this.removePlayerIfEmpty(playerId, data);
        this.save();
        return added;
    }

    public synchronized void setDescription(UUID playerId, String homeName, String description) throws IOException {
        String home = normalize(homeName);
        MutableData data = this.players.computeIfAbsent(playerId, ignored -> new MutableData());
        if (description == null || description.isBlank()) {
            data.descriptions.remove(home);
        } else {
            data.descriptions.put(home, description.strip());
        }
        this.removePlayerIfEmpty(playerId, data);
        this.save();
    }

    public synchronized PlayerGuiData snapshot(UUID playerId) {
        MutableData data = this.players.get(playerId);
        return data == null
                ? new PlayerGuiData(Set.of(), Map.of())
                : new PlayerGuiData(data.favorites, data.descriptions);
    }

    static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private void removePlayerIfEmpty(UUID playerId, MutableData data) {
        if (data.isEmpty()) {
            this.players.remove(playerId);
        }
    }

    private void load() {
        if (!Files.isRegularFile(this.file)) {
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(this.file.toFile());
        } catch (IOException | InvalidConfigurationException exception) {
            this.warningLogger.accept("Nie udało się wczytać gui-data.yml: " + exception.getMessage());
            return;
        }
        ConfigurationSection root = yaml.getConfigurationSection(PLAYERS);
        if (root == null) {
            return;
        }
        for (String rawId : root.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(rawId);
                MutableData data = new MutableData();
                yaml.getStringList(PLAYERS + "." + rawId + ".favorites").stream()
                        .filter(value -> value != null && !value.isBlank())
                        .map(GuiDataStore::normalize)
                        .forEach(data.favorites::add);
                ConfigurationSection descriptions = yaml.getConfigurationSection(PLAYERS + "." + rawId + ".descriptions");
                if (descriptions != null) {
                    for (String encoded : descriptions.getKeys(false)) {
                        decode(encoded).ifPresent(home -> {
                            String text = descriptions.getString(encoded, "").strip();
                            if (!text.isBlank()) {
                                data.descriptions.put(normalize(home), text);
                            }
                        });
                    }
                }
                if (!data.isEmpty()) {
                    this.players.put(playerId, data);
                }
            } catch (IllegalArgumentException exception) {
                this.warningLogger.accept("Pomijam nieprawidłowy UUID w gui-data.yml: " + rawId);
            }
        }
    }

    private void save() throws IOException {
        Files.createDirectories(this.file.getParent());
        YamlConfiguration yaml = new YamlConfiguration();
        this.players.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String base = PLAYERS + "." + entry.getKey();
            yaml.set(base + ".favorites", entry.getValue().favorites.stream().sorted().toList());
            entry.getValue().descriptions.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(description ->
                    yaml.set(base + ".descriptions." + encode(description.getKey()), description.getValue()));
        });
        yaml.save(this.file.toFile());
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static java.util.Optional<String> decode(String value) {
        try {
            return java.util.Optional.of(new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }

    private static final class MutableData {
        private final Set<String> favorites = new HashSet<>();
        private final Map<String, String> descriptions = new HashMap<>();

        private boolean isEmpty() {
            return this.favorites.isEmpty() && this.descriptions.isEmpty();
        }
    }
}
