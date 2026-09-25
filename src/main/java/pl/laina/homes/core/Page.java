package pl.laina.homes.core;

import java.util.List;

public record Page<T>(List<T> entries, int index, int totalPages, int totalEntries) {
    public Page {
        entries = List.copyOf(entries);
    }

    public boolean hasPrevious() {
        return this.index > 0;
    }

    public boolean hasNext() {
        return this.index + 1 < this.totalPages;
    }
}
