package me.davipccunha.tests.signshop.cache;

import me.davipccunha.tests.signshop.api.model.Shop;
import me.davipccunha.tests.signshop.api.model.ShopLocation;
import me.davipccunha.tests.signshop.util.serializer.ShopLocationSerializer;
import me.davipccunha.tests.signshop.util.serializer.ShopSerializer;
import me.davipccunha.utils.cache.RedisConnector;
import org.bukkit.Location;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ShopCache {
    private final String redisKey;
    private final RedisConnector redisConnector = new RedisConnector();

    public ShopCache(String redisKey) {
        this.redisKey = redisKey;
    }

    public void add(Shop shop) {
        try (Jedis jedis = redisConnector.getJedis()) {
            final Pipeline pipeline = jedis.pipelined();
            pipeline.hset(this.redisKey, ShopLocationSerializer.serialize(shop.getLocation()), ShopSerializer.serialize(shop));
            pipeline.sync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(ShopLocation shopLocation) {
        if (this.has(shopLocation)) {
            try (Jedis jedis = redisConnector.getJedis()) {
                final Pipeline pipeline = jedis.pipelined();
                pipeline.hdel(this.redisKey, ShopLocationSerializer.serialize(shopLocation));
                pipeline.sync();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void remove(Location location) {
        ShopLocation shopLocation = new ShopLocation(location);
        this.remove(shopLocation);
    }

    public boolean has(ShopLocation shopLocation) {
        try (Jedis jedis = redisConnector.getJedis()) {
            Pipeline pipeline = jedis.pipelined();
            Response<Boolean> response = pipeline.hexists(this.redisKey, ShopLocationSerializer.serialize(shopLocation));
            pipeline.sync();

            if (response == null || response.get() == null) return false;

            return response.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean has(Location location) {
        ShopLocation shopLocation = new ShopLocation(location);
        return this.has(shopLocation);
    }

    public Shop get(ShopLocation shopLocation) {
        try (Jedis jedis = redisConnector.getJedis()) {
            Pipeline pipeline = jedis.pipelined();
            Response<String> response = pipeline.hget(this.redisKey, ShopLocationSerializer.serialize(shopLocation));
            pipeline.sync();

            if (response == null || response.get() == null) return null;

            return ShopSerializer.deserialize(response.get());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Collection<Shop> getShops() {
        try (Jedis jedis = redisConnector.getJedis()) {
            Pipeline pipeline = jedis.pipelined();
            Response<Map<String, String>> response = pipeline.hgetAll(this.redisKey);
            pipeline.sync();

            if (response == null || response.get() == null) return null;

            return response.get().values().stream().map(ShopSerializer::deserialize).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Collection<Shop> getAdminShops() {
        return this.getShops().stream().filter(Shop::isAdminShop).collect(Collectors.toList());
    }

    public void removeGhostShops() {
        for (Shop shop : this.getAdminShops()) {
            if (shop.getShopSign() == null)
                this.remove(shop.getLocation());
        }
    }
}
