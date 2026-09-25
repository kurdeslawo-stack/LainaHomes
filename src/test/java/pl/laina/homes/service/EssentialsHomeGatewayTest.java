package pl.laina.homes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.earth2me.essentials.User;
import java.util.List;
import net.ess3.api.IEssentials;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class EssentialsHomeGatewayTest {
    @Test
    void readsExistingEssentialsHomesWithoutTakingOwnershipOfTeleporting() throws Exception {
        IEssentials essentials = mock(IEssentials.class);
        Player player = mock(Player.class);
        User user = mock(User.class);
        World world = mock(World.class);
        when(essentials.getUser(player)).thenReturn(user);
        when(user.getHomes()).thenReturn(List.of("baza"));
        when(user.getHome("baza")).thenReturn(new Location(world, 12.5, 64, -7.25));
        when(world.getName()).thenReturn("world");

        EssentialsHomeGateway gateway = new EssentialsHomeGateway(essentials, "2.22.0", ignored -> {});

        assertEquals(List.of(new pl.laina.homes.model.HomeSnapshot("baza", "world", 12.5, 64, -7.25)),
                gateway.homes(player));
        assertEquals("EssentialsX 2.22.0", gateway.backendVersion());
        verify(user).getHomes();
        verify(user).getHome("baza");
    }
}
