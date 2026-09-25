package pl.laina.homes.core;

import java.util.Comparator;
import java.util.List;
import pl.laina.homes.model.HomeEntry;

public final class HomeCatalogue {
    public Page<HomeEntry> page(List<HomeEntry> homes, int requestedPage, int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be positive");
        }
        List<HomeEntry> sorted = homes.stream()
                .sorted(Comparator.comparing(HomeEntry::favorite).reversed()
                        .thenComparing(entry -> entry.home().name(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(entry -> entry.home().name()))
                .toList();
        int totalPages = Math.max(1, (sorted.size() + pageSize - 1) / pageSize);
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int from = Math.min(sorted.size(), page * pageSize);
        int to = Math.min(sorted.size(), from + pageSize);
        return new Page<>(sorted.subList(from, to), page, totalPages, sorted.size());
    }
}
