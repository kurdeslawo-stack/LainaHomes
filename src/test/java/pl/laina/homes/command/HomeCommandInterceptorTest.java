package pl.laina.homes.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.Test;
import pl.laina.homes.gui.HomesMenuController;
import pl.laina.homes.message.Messages;

class HomeCommandInterceptorTest {
    @Test
    void cancelsBareHomeAndShowsConfiguredDenialWithoutPermission() {
        HomesMenuController menus = mock(HomesMenuController.class);
        Messages messages = mock(Messages.class);
        Player player = mock(Player.class);
        PlayerCommandPreprocessEvent event = mock(PlayerCommandPreprocessEvent.class);
        when(event.getMessage()).thenReturn("/home");
        when(event.getPlayer()).thenReturn(player);
        when(player.hasPermission("lainahomes.use")).thenReturn(false);

        new HomeCommandInterceptor(menus, messages).onCommand(event);

        verify(event).setCancelled(true);
        verify(messages).send(player, "no-permission");
        verify(menus, never()).open(player);
    }
}
