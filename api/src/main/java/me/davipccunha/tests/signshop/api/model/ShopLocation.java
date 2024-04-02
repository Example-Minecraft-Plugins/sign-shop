package me.davipccunha.tests.signshop.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
public class ShopLocation {
    private final String worldName;
    private final int x, y, z;

    public ShopLocation(Location location) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ShopLocation)) return false;

        return this.worldName.equals(((ShopLocation) obj).worldName)
                && this.x == ((ShopLocation) obj).x
                && this.y == ((ShopLocation) obj).y
                && this.z == ((ShopLocation) obj).z;

    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }

    @Override
    public String toString() {
        return worldName + " @ (" + x + ", " + y + ", " + z + ")";
    }
}