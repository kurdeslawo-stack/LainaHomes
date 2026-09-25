package pl.laina.homes;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchitectureSafetyTest {
    @Test
    void addonDoesNotTeleportOrMutateEssentialsHomesDirectly() throws IOException {
        Path sourceRoot = Path.of("src", "main", "java");
        String source;
        try (var files = Files.walk(sourceRoot)) {
            source = files.filter(path -> path.toString().endsWith(".java"))
                    .map(this::read)
                    .reduce("", (left, right) -> left + "\n" + right);
        }

        assertFalse(source.contains("player.teleport("));
        assertFalse(source.contains("player.teleportAsync("));
        assertFalse(source.contains(".setHome("));
        assertFalse(source.contains(".delHome("));
        assertFalse(source.contains(".renameHome("));
    }

    @Test
    void pluginDoesNotRegisterOrReplaceTheHomeCommand() throws IOException {
        String pluginYml = Files.readString(Path.of("src", "main", "resources", "plugin.yml"));
        assertFalse(pluginYml.lines().anyMatch(line -> line.matches("^  home:$")));
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
