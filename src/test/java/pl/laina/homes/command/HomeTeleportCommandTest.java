package pl.laina.homes.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import pl.laina.homes.service.HomeTeleportCommand;

class HomeTeleportCommandTest {
    @Test
    void delegatesToExistingHomeCommand() {
        assertEquals("home baza", HomeTeleportCommand.forHome("baza"));
        assertEquals("home dom-2", HomeTeleportCommand.forHome("dom-2"));
    }

    @Test
    void rejectsNamesThatCouldBecomeAdditionalArguments() {
        assertThrows(IllegalArgumentException.class, () -> HomeTeleportCommand.forHome("baza druga"));
        assertThrows(IllegalArgumentException.class, () -> HomeTeleportCommand.forHome("\nstop"));
        assertThrows(IllegalArgumentException.class, () -> HomeTeleportCommand.forHome("gracz:dom"));
        assertThrows(IllegalArgumentException.class, () -> HomeTeleportCommand.forHome("../dom"));
        assertThrows(IllegalArgumentException.class, () -> HomeTeleportCommand.forHome(""));
    }
}
