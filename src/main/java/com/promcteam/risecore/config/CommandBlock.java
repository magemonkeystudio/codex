package com.promcteam.risecore.config;

import com.promcteam.risecore.Core;
import com.promcteam.risecore.legacy.cmds.CommandType;
import com.promcteam.risecore.legacy.cmds.DelayedCommand;
import com.promcteam.risecore.legacy.cmds.R;
import com.promcteam.risecore.legacy.util.DeserializationWorker;
import com.promcteam.risecore.legacy.util.SerializationBuilder;
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

import java.util.*;

public class CommandBlock implements ConfigurationSerializable {
    private Material             material;
    private int                  type            = -1;
    private String               permission;
    private boolean              cancelAction    = true;
    private List<DelayedCommand> delayedCommands =
            Collections.singletonList(new DelayedCommand(CommandType.CONSOLE, "say {player}", 0));

    public CommandBlock() {
    }

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
                        final List<DelayedCommand> delayedCommands) {
        this.material = material;
        this.type = type;
        this.permission = permission;
        this.cancelAction = cancelAction;
        this.delayedCommands = delayedCommands;
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

    public void invoke(PlayerEvent event, R... reps) {
        Player player = event.getPlayer();
        if ((this.delayedCommands == null) || this.delayedCommands.isEmpty()) {
            return;
        }
        if (player.hasPermission("core.oninteract.bypass")) {
            return;
        }
        if ((this.permission != null) && !player.hasPermission(this.permission)) {
            return;
        }
        if (this.cancelAction && (event instanceof Cancellable)) {
            ((Cancellable) event).setCancelled(true);
        }
        DelayedCommand.invoke(Core.getInstance(), player, this.delayedCommands, reps);
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(final Material material) {
        this.material = material;
    }

    public boolean isCancelAction() {
        return this.cancelAction;
    }

    public void setCancelAction(final boolean cancelAction) {
        this.cancelAction = cancelAction;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(final String permission) {
        this.permission = permission;
    }

    public int getType() {
        return this.type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public List<DelayedCommand> getDelayedCommands() {
        return this.delayedCommands;
    }

    public void setDelayedCommands(final List<DelayedCommand> delayedCommands) {
        this.delayedCommands = delayedCommands;
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

    @Override
    public Map<String, Object> serialize() {
        return SerializationBuilder.start(3).append("material", this.material).append("type", this.type)
                .append("cancelAction", this.cancelAction).append("delayedCommands", this.delayedCommands).build();
    }
}
