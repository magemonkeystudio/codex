package com.promcteam.codex.legacy.item;

import com.promcteam.codex.util.SerializationBuilder;
import com.promcteam.risecore.legacy.util.DeserializationWorker;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

import java.util.Map;

@NoArgsConstructor
public class MapBuilder extends DataBuilder {
    private boolean scaling;

    public MapBuilder(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.scaling = w.getBoolean("scaling");
    }

    public boolean isScaling() {
        return this.scaling;
    }

    public MapBuilder scaling(final boolean scaling) {
        this.scaling = scaling;
        return this;
    }

    public MapBuilder enableScaling() {
        return this.scaling(true);
    }

    public MapBuilder disableScaling() {
        return this.scaling(false);
    }

    @Override
    public void apply(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof MapMeta)) {
            return;
        }

        MapMeta meta = (MapMeta) itemMeta;
        meta.setScaling(this.scaling);
    }

    @Override
    public MapBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof MapMeta)) {
            return null;
        }

        MapMeta meta = (MapMeta) itemMeta;
        this.scaling = meta.isScaling();
        return this;
    }

    @Override
    public String getType() {
        return "map";
    }

    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(2).append(super.serialize());
        b.append("scaling", this.scaling);
        return b.build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("scaling", this.scaling)
                .toString();
    }

    public static MapBuilder start() {
        return new MapBuilder();
    }
}
