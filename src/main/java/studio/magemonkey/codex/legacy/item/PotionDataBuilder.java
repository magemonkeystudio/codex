package studio.magemonkey.codex.legacy.item;

import studio.magemonkey.codex.util.SerializationBuilder;
import studio.magemonkey.risecore.legacy.util.DeserializationWorker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
@SerializableAs("Codex_PotionMeta")
public class PotionDataBuilder extends DataBuilder {
    protected Map<PotionEffectType, PotionData> potions = new LinkedHashMap<>(5);
    protected PotionEffectType                  main    = PotionEffectType.SPEED;

    public PotionDataBuilder(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.main = PotionEffectType.getByName(w.getString("main", "SPEED"));
        final Map<Object, Map<Object, Object>> effects = w.getTypedObject("effects");
        for (final Map.Entry<Object, Map<Object, Object>> entry : effects.entrySet()) {
            final PotionEffectType type = PotionEffectType.getByName(entry.getKey().toString());

            final DeserializationWorker effect  = DeserializationWorker.startUnsafe(entry.getValue());
            final byte                  power   = effect.getByte("power");
            final int                   time    = effect.getInt("time");
            final boolean               ambient = effect.getBoolean("ambient");
            this.potions.put(type, new PotionData(power, time, ambient));
        }
    }

    public PotionDataBuilder potions(final Map<PotionEffectType, PotionData> potions) {
        this.potions = potions;
        return this;
    }

    public PotionDataBuilder add(final PotionEffectType type, final byte power, final int time, final boolean ambient) {
        this.potions.put(type, new PotionData(power, time, ambient));
        return this;
    }

    public PotionDataBuilder add(final PotionEffectType type, final int power, final int time, final boolean ambient) {
        return this.add(type, (byte) power, time, ambient);
    }

    public PotionDataBuilder add(final PotionEffectType type, final byte power, final int time) {
        return this.add(type, power, time, false);
    }

    public PotionDataBuilder add(final PotionEffectType type, final int power, final int time) {
        return this.add(type, (byte) power, time);
    }

    public PotionDataBuilder add(final PotionEffectType type, final byte power) {
        return this.add(type, power, 0);
    }

    public PotionDataBuilder add(final PotionEffectType type, final int time) {
        return this.add(type, 0, time);
    }

    public PotionDataBuilder remove(final PotionEffectType type) {
        this.potions.remove(type);
        return this;
    }

    public PotionDataBuilder clear() {
        this.potions.clear();
        return this;
    }

    public PotionDataBuilder main(final PotionEffectType type) {
        this.main = type;
        return this;
    }

    @Override
    public void apply(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof PotionMeta)) {
            return;
        }

        PotionMeta meta = (PotionMeta) itemMeta;
        meta.clearCustomEffects();
        for (final Map.Entry<PotionEffectType, PotionData> entry : this.potions.entrySet()) {
            final PotionData data = entry.getValue();
            meta.addCustomEffect(new PotionEffect(entry.getKey(), data.time, data.power, data.ambient), true);
        }
        if (this.main != null) {
            meta.setMainEffect(this.main);
        }
    }

    @Override
    public PotionDataBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof PotionMeta)) {
            return null;
        }

        PotionMeta meta = (PotionMeta) itemMeta;
        if (meta.hasCustomEffects()) {
            this.main = meta.getCustomEffects().get(0).getType();
        }
        for (final PotionEffect effect : meta.getCustomEffects()) {
            this.potions.put(effect.getType(),
                    new PotionData((byte) effect.getAmplifier(), effect.getDuration(), effect.isAmbient()));
        }
        return this;
    }

    @Override
    public String getType() {
        return "potion";
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(3).append(super.serialize());
        b.append("main", this.main.getName());
        final SerializationBuilder effects = SerializationBuilder.start(this.potions.size());
        for (final Map.Entry<PotionEffectType, PotionData> entry : this.potions.entrySet()) {
            final SerializationBuilder effect     = SerializationBuilder.start(3);
            final PotionData           potionData = entry.getValue();
            effect.append("power", potionData.power);
            effect.append("time", potionData.time);
            effect.append("ambient", potionData.ambient);
            effects.append(entry.getKey().getName(), effect);
        }
        b.append("effects", effects);
        return b.build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("potions", this.potions)
                .append("main", this.main)
                .toString();
    }

    public class PotionData {
        private final byte    power;
        private final int     time;
        private final boolean ambient;

        public PotionData(final byte power, final int time, final boolean ambient) {
            this.power = power;
            this.time = time;
            this.ambient = ambient;
        }

        public byte getPower() {
            return this.power;
        }

        public int getTime() {
            return this.time;
        }

        public boolean isAmbient() {
            return this.ambient;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                    .append("power", this.power)
                    .append("time", this.time)
                    .append("ambient", this.ambient)
                    .toString();
        }
    }

    public static PotionDataBuilder start() {
        return new PotionDataBuilder();
    }

}
