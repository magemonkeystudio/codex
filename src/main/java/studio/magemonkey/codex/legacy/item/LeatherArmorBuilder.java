package studio.magemonkey.codex.legacy.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.Color;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import studio.magemonkey.codex.util.ItemUtils;
import studio.magemonkey.codex.util.SerializationBuilder;
import studio.magemonkey.risecore.legacy.util.DeserializationWorker;

import java.util.Map;

@Getter
@NoArgsConstructor
@SerializableAs("Codex_LeatherArmorMeta")
public class LeatherArmorBuilder extends DataBuilder {
    private int red, green, blue;

    public LeatherArmorBuilder(final Map<String, Object> map) {
        final DeserializationWorker w     = DeserializationWorker.start(map);
        final Color                 color = ItemUtils.simpleDeserializeColor(w.getString("color"));
        if (color == null) {
            return;
        }
        this.color(color);
    }

    public LeatherArmorBuilder red(final int red) {
        this.red = red;
        return this;
    }

    public LeatherArmorBuilder green(final int green) {
        this.green = green;
        return this;
    }

    public LeatherArmorBuilder blue(final int blue) {
        this.blue = blue;
        return this;
    }

    public LeatherArmorBuilder color(final int red, final int green, final int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    public LeatherArmorBuilder color(final int[] rgb) {
        if (rgb.length != 3) {
            throw new IllegalArgumentException("size of rgb array must be: 3");
        }
        this.red = rgb[0];
        this.green = rgb[1];
        this.blue = rgb[2];
        return this;
    }

    public LeatherArmorBuilder color(final Color color) {
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        return this;
    }

    @Override
    public void apply(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof LeatherArmorMeta)) {
            return;
        }
        LeatherArmorMeta meta = (LeatherArmorMeta) itemMeta;
        meta.setColor(Color.fromRGB(this.red, this.green, this.blue));
    }

    @Override
    public LeatherArmorBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof LeatherArmorMeta)) {
            return null;
        }
        LeatherArmorMeta meta = (LeatherArmorMeta) itemMeta;
        return (meta.getColor() != null) ? this.color(meta.getColor()) : this;
    }

    @Override
    public String getType() {
        return "leather";
    }

    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(2).append(super.serialize());
        b.append("color", ItemUtils.simpleSerializeColor(Color.fromRGB(this.red, this.green, this.blue)));
        return b.build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("red", this.red)
                .append("green", this.green)
                .append("blue", this.blue)
                .toString();
    }

    public static LeatherArmorBuilder start() {
        return new LeatherArmorBuilder();
    }
}
