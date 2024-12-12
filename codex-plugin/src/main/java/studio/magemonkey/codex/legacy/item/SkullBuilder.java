package studio.magemonkey.codex.legacy.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.legacy.utils.Utils;
import studio.magemonkey.codex.util.DeserializationWorker;
import studio.magemonkey.codex.util.SerializationBuilder;

import java.util.Map;

@Getter
@NoArgsConstructor
@SerializableAs("Codex_SkullMeta")
public class SkullBuilder extends DataBuilder {
    private String owner;

    public SkullBuilder(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.owner = w.getString("owner");
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
        meta.setOwner(Utils.fixColors(this.owner));
    }

    @Override
    public SkullBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof SkullMeta)) {
            return null;
        }

        SkullMeta meta = (SkullMeta) itemMeta;
        this.owner = Utils.removeColors(meta.getOwner());
        return this;
    }

    @Override
    public String getType() {
        return "skull";
    }

    @NotNull
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
