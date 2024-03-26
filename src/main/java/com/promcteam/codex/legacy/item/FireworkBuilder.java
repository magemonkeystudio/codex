package com.promcteam.codex.legacy.item;

import com.promcteam.codex.util.ItemUtils;
import com.promcteam.codex.util.SerializationBuilder;
import com.promcteam.risecore.legacy.util.DeserializationWorker;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class FireworkBuilder extends DataBuilder {
    private List<FireworkEffect> effects = new ArrayList<>(10);
    private int                  power;

    public FireworkBuilder() {
    }

    @SuppressWarnings("unchecked")
    public FireworkBuilder(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.power = w.getInt("power", 0);
        this.effects = ItemUtils.simpleDeserializeEffects((Collection<Map<Object, Object>>) map.get("effects"));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("effects", this.effects)
                .append("power", this.power)
                .toString();
    }

    public FireworkBuilder power(final int power) {
        this.power = power;
        return this;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public FireworkBuilder effect(final FireworkEffect effect) {
        this.effects.add(effect);
        return this;
    }

    public FireworkBuilder effect(final FireworkEffect.Builder effect) {
        this.effects.add(effect.build());
        return this;
    }

    public FireworkBuilder effect(final FireworkEffectBuilder effect) {
        this.effects.add(effect.getEffect());
        return this;
    }

    public FireworkBuilder effect(final FireworkEffect... effects) {
        Collections.addAll(this.effects, effects);
        return this;
    }

    public FireworkBuilder effect(final FireworkEffect.Builder... effects) {
        for (final FireworkEffect.Builder effect : effects) {
            this.effects.add(effect.build());
        }
        return this;
    }

    public FireworkBuilder effect(final FireworkEffectBuilder... effects) {
        for (final FireworkEffectBuilder effect : effects) {
            this.effects.add(effect.getEffect());
        }
        return this;
    }

    public FireworkBuilder effect(final Collection<FireworkEffect> effects) {
        this.effects.addAll(effects);
        return this;
    }

    public FireworkBuilder remove(final FireworkEffect effect) {
        this.effects.remove(effect);
        return this;
    }

    public FireworkBuilder remove(final FireworkEffect.Builder effect) {
        this.effects.remove(effect.build());
        return this;
    }

    public FireworkBuilder remove(final FireworkEffectBuilder effect) {
        this.effects.remove(effect.getEffect());
        return this;
    }

    public FireworkBuilder remove(final int effect) {
        this.effects.remove(effect);
        return this;
    }

    public FireworkBuilder clear() {
        this.effects.clear();
        return this;
    }

    @Override
    public void apply(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof FireworkMeta)) {
            return;
        }

        FireworkMeta meta = (FireworkMeta) itemMeta;
        meta.setPower(this.power);
        meta.clearEffects();
        meta.addEffects(this.effects);
    }

    @Override
    public FireworkBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof FireworkMeta)) {
            return null;
        }

        FireworkMeta meta = (FireworkMeta) itemMeta;
        this.power = meta.getPower();
        this.effects = new ArrayList<>(meta.getEffects());
        return this;
    }

    @Override
    public String getType() {
        return "firework";
    }

    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(3).append(super.serialize());
        b.append("power", this.power);
        b.append("effects", ItemUtils.simpleSerializeEffects(this.effects));
        return b.build();
    }

    public static FireworkBuilder start() {
        return new FireworkBuilder();
    }
}
