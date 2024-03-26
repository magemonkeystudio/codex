package com.promcteam.codex.legacy.item;

import com.promcteam.codex.util.ItemUtils;
import com.promcteam.codex.util.SerializationBuilder;
import com.promcteam.risecore.legacy.util.DeserializationWorker;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

@NoArgsConstructor
public class SkullBuilder extends DataBuilder {
    private String owner;

    public SkullBuilder(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.owner = w.getString("owner");
    }

    public String getOwner() {
        return this.owner;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public SkullBuilder owner(final String owner) {
        this.owner = owner;
        return this;
    }

    public SkullBuilder clear() {
        this.owner = null;
        return this;
    }

    @Override
    public void apply(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof SkullMeta)) {
            return;
        }

        SkullMeta meta = (SkullMeta) itemMeta;
        meta.setOwner(ItemUtils.fixColors(this.owner));
    }

    @Override
    public SkullBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof SkullMeta)) {
            return null;
        }

        SkullMeta meta = (SkullMeta) itemMeta;
        this.owner = ItemUtils.removeColors(meta.getOwner());
        return this;
    }

    @Override
    public String getType() {
        return "skull";
    }

    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(2).append(super.serialize());
        b.append("owner", this.owner);
        return b.build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("owner", this.owner)
                .toString();
    }

    public static SkullBuilder start() {
        return new SkullBuilder();
    }
}
