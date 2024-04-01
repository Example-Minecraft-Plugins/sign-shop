package me.davipccunha.tests.signshop.cache;

import me.davipccunha.tests.signshop.api.Shop;
import me.davipccunha.tests.signshop.api.ShopLocation;
import me.davipccunha.tests.signshop.util.serializer.ShopLocationSerializer;
import me.davipccunha.tests.signshop.util.serializer.ShopSerializer;
import org.bukkit.Location;

import java.util.Collection;
import java.util.stream.Collectors;

public class ShopCache {
    private final RedisConnector redisConnector = new RedisConnector("localhost", 6379, "davi123");

    public void add(Shop shop) {
        redisConnector.getJedis().hset("shops", ShopLocationSerializer.serialize(shop.getLocation()), ShopSerializer.serialize(shop));
    }

    public void remove(ShopLocation shopLocation) {
        if (this.has(shopLocation))
            redisConnector.getJedis().hdel("shops", ShopLocationSerializer.serialize(shopLocation));
    }

    public void remove(Location location) {
        ShopLocation shopLocation = new ShopLocation(location);
        this.remove(shopLocation);
    }

    public boolean has(ShopLocation shopLocation) {
        return redisConnector.getJedis().hexists("shops", ShopLocationSerializer.serialize(shopLocation));
    }

    public Shop get(ShopLocation shopLocation) {
        return ShopSerializer.deserialize(redisConnector.getJedis().hget("shops", ShopLocationSerializer.serialize(shopLocation)));
    }

    public Collection<Shop> getShops() {
        return redisConnector.getJedis().hgetAll("shops").values().stream().map(ShopSerializer::deserialize).collect(Collectors.toList());
    }

    public Collection<Shop> getAdminShops() {
        return this.getShops().stream().filter(Shop::isAdminShop).collect(Collectors.toList());
    }
}
