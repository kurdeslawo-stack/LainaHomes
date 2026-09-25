package pl.laina.homes.message;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {
    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void send(CommandSender sender, String key) {
        this.send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String prefix = this.plugin.getConfig().getString("messages.prefix", "");
        String value = this.plugin.getConfig().getString("messages." + key, "<red>Brak wiadomości: " + key);
        sender.sendMessage(this.parse(prefix + value, placeholders));
    }

    public Component parse(String template) {
        return this.parse(template, Map.of());
    }

    public Component parse(String template, Map<String, String> placeholders) {
        String rendered = template == null ? "" : template;
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            String safe = this.miniMessage.escapeTags(placeholder.getValue() == null ? "" : placeholder.getValue());
            rendered = rendered.replace("%" + placeholder.getKey() + "%", safe);
        }
        return this.miniMessage.deserialize(rendered);
    }

    public String escape(String value) {
        return this.miniMessage.escapeTags(value == null ? "" : value);
    }
}
