package studio.magemonkey.codex;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import studio.magemonkey.codex.testutil.MockedTest;

public class CommandTest extends MockedTest {
    private PlayerMock player;

    @BeforeEach
    public void setup() {
        player = genPlayer("Travja");
    }

    @Test
    public void testHelpText() {
        player.performCommand("codex help");
        player.assertSaid("§8§m━━━━━━━━━━━━§8§l[ §e§lCodexCore §7- §6§lHelp §8§l]§8§m━━━━━━━━━━━━");
        player.assertSaid("§6» §e/codex help §7- Show help page.");
        player.assertSaid("§6» §e/codex reload §7- Reload the plugin.");
    }

}
