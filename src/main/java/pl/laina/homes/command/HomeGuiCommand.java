package pl.laina.homes.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.laina.homes.LainaHomesPlugin;
import pl.laina.homes.gui.HomesMenuController;
import pl.laina.homes.message.Messages;

public final class HomeGuiCommand implements CommandExecutor, TabCompleter {
    private final LainaHomesPlugin plugin;
    private final HomesMenuController menus;
    private final Messages messages;

    public HomeGuiCommand(LainaHomesPlugin plugin, HomesMenuController menus, Messages messages) {
        this.plugin = plugin;
        this.menus = menus;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("homes") || args.length == 0) {
            return this.open(sender);
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("lainahomes.reload")) {
                this.messages.send(sender, "no-permission");
                return true;
            }
            this.plugin.reloadSettings();
            this.messages.send(sender, "reloaded");
            return true;
        }
        if (args[0].equalsIgnoreCase("debug")) {
            if (!sender.hasPermission("lainahomes.debug")) {
                this.messages.send(sender, "no-permission");
                return true;
            }
            sender.sendMessage(this.messages.parse("<gray>Backend: <white>" + this.menus.backendVersion()));
            if (sender instanceof Player player) {
                sender.sendMessage(this.messages.parse("<gray>Home'y z API: <white>" + this.menus.homeCount(player)));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("description")) {
            return this.description(sender, args);
        }
        this.messages.send(sender, "description-usage");
        return true;
    }

    private boolean open(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            this.messages.send(sender, "players-only");
            return true;
        }
        if (!player.hasPermission("lainahomes.use")) {
            this.messages.send(player, "no-permission");
            return true;
        }
        this.menus.open(player);
        return true;
    }

    private boolean description(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            this.messages.send(sender, "players-only");
            return true;
        }
        if (!player.hasPermission("lainahomes.description")) {
            this.messages.send(player, "no-permission");
            return true;
        }
        if (args.length < 3) {
            this.messages.send(player, "description-usage");
            return true;
        }
        String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        this.menus.setDescription(player, args[1], description.equals("-") ? "" : description);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("homes")) {
            return List.of();
        }
        if (args.length == 1) {
            ArrayList<String> options = new ArrayList<>();
            if (sender.hasPermission("lainahomes.description")) {
                options.add("description");
            }
            if (sender.hasPermission("lainahomes.reload")) {
                options.add("reload");
            }
            if (sender.hasPermission("lainahomes.debug")) {
                options.add("debug");
            }
            return filter(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("description") && sender instanceof Player player) {
            return filter(this.menus.homeNames(player), args[1]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> values, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(normalized)).toList();
    }
}
