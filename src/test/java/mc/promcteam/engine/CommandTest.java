package mc.promcteam.engine;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import mc.promcteam.engine.testutil.MockedTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandTest extends MockedTest {

    private Logger     log = LoggerFactory.getLogger(CommandTest.class);
    private PlayerMock player;

    @BeforeEach
    public void setup() {
        player = genPlayer("Travja");
    }

    @Test
    public void testHelpText() {
        player.performCommand("nexengine help");
        player.assertSaid("§8§m━━━━━━━━━━━━§8§l[ §e§lProMCCore §7- §6§lHelp §8§l]§8§m━━━━━━━━━━━━");
        player.assertSaid("§6» §e/nexengine help §7- Show help page.");
        player.assertSaid("§6» §e/nexengine base64 §7- Converts item to Base64");
        player.assertSaid("§6» §e/nexengine reload §7- Reload the plugin.");
    }

}
