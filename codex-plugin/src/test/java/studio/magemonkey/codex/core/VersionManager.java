package studio.magemonkey.codex.core;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import studio.magemonkey.codex.api.NMS;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
public class VersionManager {
    private final NMS nms;

    public VersionManager(JavaPlugin plugin) {
        this.nms = mock(NMS.class);
        when(this.nms.getVersion()).thenReturn("test");
        when(this.nms.fixColors(anyString())).thenAnswer(ans -> ans.getArgument(0));

        plugin.getLogger().info("Using NMS implementation for version " + nms.getVersion());
    }
}
