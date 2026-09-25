package pl.laina.homes.model;

import java.util.Objects;

public record HomeEntry(HomeSnapshot home, boolean favorite, String description) {
    public HomeEntry {
        Objects.requireNonNull(home, "home");
        description = description == null ? "" : description;
    }
}
