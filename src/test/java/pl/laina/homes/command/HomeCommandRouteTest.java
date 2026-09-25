package pl.laina.homes.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HomeCommandRouteTest {
    @Test
    void onlyBareHomeOpensTheGui() {
        assertTrue(HomeCommandRoute.opensGui("/home"));
        assertTrue(HomeCommandRoute.opensGui("  /HOME  "));
        assertFalse(HomeCommandRoute.opensGui("/home baza"));
        assertFalse(HomeCommandRoute.opensGui("/homes"));
        assertFalse(HomeCommandRoute.opensGui("/essentials:home"));
    }
}
