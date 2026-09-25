package pl.laina.homes.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.laina.homes.model.HomeEntry;
import pl.laina.homes.model.HomeSnapshot;

class HomeCatalogueTest {
    private final HomeCatalogue catalogue = new HomeCatalogue();

    @Test
    void supportsZeroAndOneHome() {
        Page<HomeEntry> empty = this.catalogue.page(List.of(), 0, 28);
        Page<HomeEntry> one = this.catalogue.page(List.of(home("baza", false)), 0, 28);

        assertEquals(1, empty.totalPages());
        assertEquals(0, empty.totalEntries());
        assertEquals(List.of("baza"), names(one));
    }

    @Test
    void favoritesComeFirstAndGroupsAreAlphabetical() {
        List<HomeEntry> homes = List.of(
                home("zwykly-z", false),
                home("ulubiony-z", true),
                home("Ulubiony-a", true),
                home("zwykly-a", false)
        );

        assertEquals(
                List.of("Ulubiony-a", "ulubiony-z", "zwykly-a", "zwykly-z"),
                names(this.catalogue.page(homes, 0, 28))
        );
    }

    @Test
    void paginatesManyHomesAndClampsPage() {
        ArrayList<HomeEntry> homes = new ArrayList<>();
        for (int index = 0; index < 35; index++) {
            homes.add(home("home-%02d".formatted(index), false));
        }

        Page<HomeEntry> first = this.catalogue.page(homes, 0, 28);
        Page<HomeEntry> second = this.catalogue.page(homes, 1, 28);
        Page<HomeEntry> clamped = this.catalogue.page(homes, 99, 28);

        assertEquals(28, first.entries().size());
        assertTrue(first.hasNext());
        assertEquals(7, second.entries().size());
        assertTrue(second.hasPrevious());
        assertFalse(second.hasNext());
        assertEquals(second, clamped);
    }

    private static List<String> names(Page<HomeEntry> page) {
        return page.entries().stream().map(entry -> entry.home().name()).toList();
    }

    private static HomeEntry home(String name, boolean favorite) {
        return new HomeEntry(new HomeSnapshot(name, "world", 1, 2, 3), favorite, "");
    }
}
