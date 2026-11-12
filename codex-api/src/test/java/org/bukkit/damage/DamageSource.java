package org.bukkit.damage;

import org.bukkit.entity.Entity;

/**
 * Test stand-in for the Bukkit class of this name. See {@link DamageType}.
 */
public final class DamageSource {
    private final DamageType type;
    private final Entity     causing;
    private final Entity     direct;

    public DamageSource(DamageType type, Entity causing, Entity direct) {
        this.type = type;
        this.causing = causing;
        this.direct = direct;
    }

    public static Builder builder(DamageType type) {
        return new Builder(type);
    }

    public DamageType getDamageType() {
        return type;
    }

    public Entity getCausingEntity() {
        return causing;
    }

    public Entity getDirectEntity() {
        return direct;
    }

    public static final class Builder {
        private final DamageType type;
        private       Entity     causing;
        private       Entity     direct;

        private Builder(DamageType type) {
            this.type = type;
        }

        public Builder withCausingEntity(Entity entity) {
            this.causing = entity;
            return this;
        }

        public Builder withDirectEntity(Entity entity) {
            this.direct = entity;
            return this;
        }

        public DamageSource build() {
            return new DamageSource(type, causing, direct);
        }
    }
}
