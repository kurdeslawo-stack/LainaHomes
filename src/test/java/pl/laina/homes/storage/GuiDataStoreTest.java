package pl.laina.homes.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GuiDataStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void favoritesAndDescriptionsSurviveRestart() throws Exception {
        Path file = this.temporaryDirectory.resolve("gui-data.yml");
        UUID player = UUID.randomUUID();
        GuiDataStore first = new GuiDataStore(file, message -> {});

        assertTrue(first.toggleFavorite(player, "Baza"));
        first.setDescription(player, "Baza", "główna baza survivalowa");

        GuiDataStore restarted = new GuiDataStore(file, message -> {});
        PlayerGuiData data = restarted.reconcile(player, Set.of("BAZA"));
        assertTrue(data.isFavorite("baza"));
        assertEquals("główna baza survivalowa", data.description("BAZA"));
        assertFalse(restarted.toggleFavorite(player, "baza"));
    }

    @Test
    void removesMetadataWhenHomeIsDeletedOrRenamed() throws Exception {
        Path file = this.temporaryDirectory.resolve("gui-data.yml");
        UUID player = UUID.randomUUID();
        GuiDataStore store = new GuiDataStore(file, message -> {});
        store.toggleFavorite(player, "stara-nazwa");
        store.setDescription(player, "stara-nazwa", "opis");

        PlayerGuiData reconciled = store.reconcile(player, Set.of("nowa-nazwa"));

        assertTrue(reconciled.favorites().isEmpty());
        assertTrue(reconciled.descriptions().isEmpty());
        GuiDataStore restarted = new GuiDataStore(file, new ArrayList<String>()::add);
        assertTrue(restarted.snapshot(player).favorites().isEmpty());
    }
}
