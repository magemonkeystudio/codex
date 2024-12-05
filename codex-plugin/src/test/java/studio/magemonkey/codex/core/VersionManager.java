package studio.magemonkey.codex.core;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import studio.magemonkey.codex.api.NMS;
import studio.magemonkey.codex.api.NMSProvider;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
public class VersionManager {

    public VersionManager(JavaPlugin plugin) {
        NMS nms = mock(NMS.class);
        when(nms.getVersion()).thenReturn("test");
        when(nms.fixColors(anyString())).thenAnswer(ans -> ans.getArgument(0));

        NMSProvider.setNms(nms);

        plugin.getLogger().info("Using NMS implementation for version " + nms.getVersion());
    }
}
