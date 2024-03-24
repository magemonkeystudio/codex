package com.promcteam.risecore.util;

import com.promcteam.risecore.legacy.util.DeserializationWorker;
import com.promcteam.risecore.legacy.util.SerializationBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@RequiredArgsConstructor
@SerializableAs("Enigma_BlockLocation")
public class BlockLocation implements ConfigurationSerializable {
    public static final BlockLocation ZERO = new BlockLocation(0, 0, 0);
    protected final     int           x;
    protected final     int           y;
    protected final     int           z;

    public BlockLocation(final Block block) {
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    public BlockLocation(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.x = w.getInt("x");
        this.y = w.getInt("y");
        this.z = w.getInt("z");
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public BlockLocation addX(final int x) {
        return new BlockLocation(this.x + x, this.y, this.z);
    }

    public BlockLocation addY(final int y) {
        return new BlockLocation(this.x, this.y + y, this.z);
    }

    public BlockLocation addZ(final int z) {
        return new BlockLocation(this.x, this.y, this.z + z);
    }

    public BlockLocation add(final int x, final int y, final int z) {
        return new BlockLocation(this.x + x, this.y + y, this.z + z);
    }

    public BlockLocation add(final BlockLocation loc) {
        return new BlockLocation(this.x + loc.x, this.y + loc.y, this.z + loc.z);
    }

    public BlockLocation subtractX(final int x) {
        return new BlockLocation(this.x - x, this.y, this.z);
    }

    public BlockLocation subtractY(final int y) {
        return new BlockLocation(this.x, this.y - y, this.z);
    }

    public BlockLocation subtractZ(final int z) {
        return new BlockLocation(this.x, this.y, this.z - z);
    }

    public BlockLocation subtract(final int x, final int y, final int z) {
        return new BlockLocation(this.x - x, this.y - y, this.z - z);
    }

    public BlockLocation subtract(final BlockLocation loc) {
        return new BlockLocation(this.x - loc.x, this.y - loc.y, this.z - loc.z);
    }


    public double length() {
        return Math.sqrt(this.lengthSquared());
    }

    public double lengthSquared() {
        return (this.x * this.x) + (this.y * this.y) + (this.z * this.z);
    }

    public double distance(final double x, final double y, final double z) {
        return Math.sqrt(this.distanceSquared(x, y, z));
    }

    public double distanceFromCenter(final double x, final double y, final double z) {
        return Math.sqrt(this.distanceSquaredFromCenter(x, y, z));
    }

    public double distance(final BlockLocation location) {
        return Math.sqrt(this.distanceSquared(location));
    }

    public double distanceSquared(final double x, final double y, final double z) {
        final double deltaX = (double) this.x - x;
        final double deltaY = (double) this.y - y;
        final double deltaZ = (double) this.z - z;
        return (deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ);
    }

    @SuppressWarnings("MagicNumber")
    public double distanceSquaredFromCenter(final double x, final double y, final double z) {
        final double deltaX = ((double) this.x + 0.5) - x;
        final double deltaY = ((double) this.y + 0.5) - y;
        final double deltaZ = ((double) this.z + 0.5) - z;
        return (deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ);
    }

    public double distanceSquared(final BlockLocation location) {
        return this.distanceSquared(location.getX(), location.getY(), location.getZ());
    }

    public BlockLocation crossProduct(final BlockLocation location) {
        return new BlockLocation((this.y * location.getZ()) - (this.z * location.getY()),
                (this.z * location.getX()) - (this.x * location.getZ()),
                (this.x * location.getY()) - (this.y * location.getX()));
    }

    public boolean isInAABB(final BlockLocation min, final BlockLocation max) {
        return (this.x >= min.x) && (this.x <= max.x) && (this.y >= min.y) && (this.y <= max.y) && (this.z >= min.z)
                && (this.z <= max.z);
    }

    public boolean isInSphere(final BlockLocation origin, final double radius) {
        return (square(origin.x - this.x) + square(origin.y - this.y) + square(origin.z - this.z)) <= square(radius);
    }

    @Override
    public Map<String, Object> serialize() {
        return SerializationBuilder.start(3).append("x", this.x).append("y", this.y).append("z", this.z).build();
    }

    private static int square(final int x) {
        return x * x;
    }

    private static double square(final double x) {
        return x * x;
    }

    @SuppressWarnings("MagicNumber")
    public long asLong() {
        return ((((long) this.x) & 0x3FFFFFF) << 38) | ((((long) this.y) & 0xFFF) << 26) | (((long) this.z)
                & 0x3FFFFFF);
    }

    @SuppressWarnings("MagicNumber")
    public static BlockLocation fromLong(final long pos) {
        final int x = (int) (pos >> 38);
        final int y = (int) ((pos >> 26) & 0xFFF);
        final int z = (int) ((pos << 38) >> 38);
        return new BlockLocation(x, y, z);
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = (31 * result) + this.y;
        result = (31 * result) + this.z;
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockLocation)) {
            return false;
        }

        BlockLocation loc = (BlockLocation) o;
        return (this.x == loc.x) && (this.y == loc.y) && (this.z == loc.z);

    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("x", this.x)
                .append("y", this.y)
                .append("z", this.z)
                .toString();
    }

    public static BlockLocation fromBlock(Block block) {
        return new BlockLocation(block.getX(), block.getY(), block.getZ());
    }

}
