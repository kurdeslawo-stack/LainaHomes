package pl.laina.homes;

import net.ess3.api.IEssentials;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.laina.homes.command.HomeCommandInterceptor;
import pl.laina.homes.command.HomeGuiCommand;
import pl.laina.homes.config.GuiSettings;
import pl.laina.homes.core.HomeCatalogue;
import pl.laina.homes.gui.GuiListener;
import pl.laina.homes.gui.DescriptionEditor;
import pl.laina.homes.gui.HomesMenuController;
import pl.laina.homes.gui.HomesMenuRenderer;
import pl.laina.homes.message.Messages;
import pl.laina.homes.service.EssentialsHomeGateway;
import pl.laina.homes.service.EssentialsBackend;
import pl.laina.homes.storage.GuiDataStore;

public final class LainaHomesPlugin extends JavaPlugin {
    private GuiSettings settings;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadSettings();

        Plugin dependency = this.getServer().getPluginManager().getPlugin("Essentials");
        IEssentials essentials = EssentialsBackend.resolve(dependency).orElse(null);
        if (essentials == null) {
            this.getLogger().severe("EssentialsX nie jest zainstalowany lub aktywny. Wyłączam LainaHomes bez dotykania danych home'ów.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Messages messages = new Messages(this);
        EssentialsHomeGateway gateway = new EssentialsHomeGateway(
                essentials,
                dependency.getPluginMeta().getVersion(),
                this.getLogger()::warning
        );
        GuiDataStore dataStore = new GuiDataStore(this.getDataFolder().toPath().resolve("gui-data.yml"), this.getLogger()::warning);
        HomesMenuRenderer renderer = new HomesMenuRenderer(messages, new HomeCatalogue());
        HomesMenuController menus = new HomesMenuController(this, gateway, dataStore, renderer, messages);
        DescriptionEditor descriptions = new DescriptionEditor(this, menus, messages);
        HomeGuiCommand executor = new HomeGuiCommand(this, menus, messages);
        this.configureCommand("homes", executor);
        this.configureCommand("homegui", executor);
        this.getServer().getPluginManager().registerEvents(new HomeCommandInterceptor(menus, messages), this);
        this.getServer().getPluginManager().registerEvents(descriptions, this);
        this.getServer().getPluginManager().registerEvents(
                new GuiListener(menus, descriptions, () -> this.settings.spamDelayMillis()), this);
        this.getLogger().info("LainaHomes włączony. Backend: " + gateway.backendVersion() + ". /home bez argumentów otwiera GUI.");
    }

    public void reloadSettings() {
        this.reloadConfig();
        this.settings = GuiSettings.load(this.getConfig(), this.getLogger()::warning);
    }

    public GuiSettings settings() {
        return this.settings;
    }

    private void configureCommand(String name, HomeGuiCommand executor) {
        PluginCommand command = this.getCommand(name);
        if (command == null) {
            throw new IllegalStateException("Brakuje komendy " + name + " w plugin.yml.");
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
