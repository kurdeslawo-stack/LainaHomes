package pl.laina.homes.model;

import java.util.Objects;

public record HomeSnapshot(String name, String world, double x, double y, double z) {
    public HomeSnapshot {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(world, "world");
    }
}
