package studio.magemonkey.codex.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import studio.magemonkey.codex.api.CommandBlock;
import studio.magemonkey.codex.config.api.JYML;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InteractListener implements Listener {
    private final List<CommandBlock> onInteract = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public InteractListener(JYML config) {
        List<Map<String, Object>> interactList = (List<Map<String, Object>>) config.get("onInteract");
        if (interactList != null) {
            for (Map<String, Object> cmd : interactList) {
                onInteract.add(new CommandBlock(cmd));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) return;

        getOnInteract(block.getType()).forEach(commandBlock -> commandBlock.invoke(e));
    }

    private List<CommandBlock> getOnInteract(Material type) {
        return onInteract.stream()
                .filter(cmd -> cmd.getMaterial() == type)
                .collect(Collectors.toList());
    }
}
