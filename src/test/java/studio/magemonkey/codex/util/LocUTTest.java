package studio.magemonkey.codex.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.magemonkey.codex.testutil.MockedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocUTTest extends MockedTest {

    @BeforeEach
    public void setup() {
        // Create a couple of blocks, one with a carpet on top, the other with just air
        World world = server.addSimpleWorld("test");
        world.getBlockAt(0, 10, 0).setType(Material.STONE);
        world.getBlockAt(0, 11, 0).setType(Material.BLUE_CARPET);

        world.getBlockAt(1, 15, 0).setType(Material.STONE);
    }

    @Test
    void getFirstGroundBlock_airFalse() {
        // Test with air = false
        assertEquals(11, LocUT.getFirstGroundBlock(new Location(server.getWorld("test"), 0, 20, 0), false).getY());
        assertEquals(16, LocUT.getFirstGroundBlock(new Location(server.getWorld("test"), 1, 20, 0), false).getY());
    }

    @Test
    void getFirstGroundBlock_airTrue() {
        // Test with air = true
        assertEquals(12, LocUT.getFirstGroundBlock(new Location(server.getWorld("test"), 0, 20, 0), true).getY());
        assertEquals(16, LocUT.getFirstGroundBlock(new Location(server.getWorld("test"), 1, 20, 0), true).getY());
    }
}