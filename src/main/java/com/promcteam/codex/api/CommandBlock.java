package com.promcteam.codex.api;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.util.SerializationBuilder;
import com.promcteam.risecore.legacy.util.DeserializationWorker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a block with a set of commands to be executed on interaction
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommandBlock implements ConfigurationSerializable {
    private Material             material;
    private int                  type            = -1;
    private String               permission;
    private boolean              cancelAction    = true;
    private List<DelayedCommand> delayedCommands =
            Collections.singletonList(new DelayedCommand(CommandType.CONSOLE, "say {player}", 0));

    public CommandBlock(Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.material = w.getEnum("material", Material.AIR);
        this.type = w.getInt("type", -1);
        this.cancelAction = w.getBoolean("cancelAction", true);
        this.delayedCommands = DelayedCommand.deserializeMapList(w.getTypedObject("delayedCommands"));
        Validate.notEmpty(this.delayedCommands, "Commands can't be empty! " + this);
    }

    public CommandBlock(final Material material,
                        final int type,
                        final String permission,
                        final boolean cancelAction,
                        DelayedCommand... delayedCommands) {
        this.material = material;
        this.type = type;
        this.permission = permission;
        this.cancelAction = cancelAction;
        this.delayedCommands = new ArrayList<>(Arrays.asList(delayedCommands));
    }

    public void invoke(PlayerEvent event, Replacer... reps) {
        Player player = event.getPlayer();
        if ((this.delayedCommands == null) || this.delayedCommands.isEmpty()) {
            return;
        }
        if (player.hasPermission("general.oninteract.bypass")) {
            return;
        }
        if ((this.permission != null) && !player.hasPermission(this.permission)) {
            return;
        }
        if (this.cancelAction && (event instanceof Cancellable)) {
            ((Cancellable) event).setCancelled(true);
        }
        DelayedCommand.invoke(CodexEngine.get(), player, this.delayedCommands, reps);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CommandBlock)) {
            return false;
        }

        CommandBlock block = (CommandBlock) o;
        return new EqualsBuilder().append(this.type, block.type)
                .append(this.material, block.material)
                .append(this.delayedCommands, block.delayedCommands)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.material)
                .append(this.type)
                .append(this.delayedCommands)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("material", this.material)
                .append("type", this.type)
                .append("delayedCommands", this.delayedCommands)
                .toString();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return SerializationBuilder.start(3).append("material", this.material).append("type", this.type)
                .append("cancelAction", this.cancelAction).append("delayedCommands", this.delayedCommands).build();
    }
}
