package studio.magemonkey.codex.legacy.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.ItemUtils;
import studio.magemonkey.codex.util.SerializationBuilder;
import studio.magemonkey.risecore.legacy.util.DeserializationWorker;

import java.util.Map;

@Getter
@NoArgsConstructor
@SerializableAs("Codex_FireworkEffectMeta")
public class FireworkEffectBuilder extends DataBuilder {
    private FireworkEffect effect;

    public FireworkEffectBuilder(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.effect = ItemUtils.simpleDeserializeEffect(w.getTypedObject("effect"));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("effect", this.effect)
                .toString();
    }

    public FireworkEffectBuilder effect(final FireworkEffect effect) {
        this.effect = effect;
        return this;
    }

    public FireworkEffectBuilder effect(final FireworkEffect.Builder effect) {
        this.effect = effect.build();
        return this;
    }

    @Override
    public void apply(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof FireworkEffectMeta)) {
            return;
        }

        FireworkEffectMeta meta = (FireworkEffectMeta) itemMeta;
        meta.setEffect(this.effect);
    }

    @Override
    public FireworkEffectBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof FireworkEffectMeta)) {
            return null;
        }

        FireworkEffectMeta meta = (FireworkEffectMeta) itemMeta;
        this.effect = meta.getEffect();
        return this;
    }

    @Override
    public String getType() {
        return "firework_effect";
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(2).append(super.serialize());
        b.append("effect", ItemUtils.simpleSerializeEffect(this.effect));
        return b.build();
    }

    public static FireworkEffectBuilder start() {
        return new FireworkEffectBuilder();
    }
}
