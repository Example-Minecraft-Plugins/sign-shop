package me.davipccunha.tests.signshop.provider;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.api.Shop;
import me.davipccunha.tests.signshop.api.ShopLocation;
import me.davipccunha.tests.signshop.api.SignShopAPI;
import me.davipccunha.tests.signshop.cache.ShopCache;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.Collection;

@RequiredArgsConstructor
public class SignShopProvider implements SignShopAPI {
    private final ShopCache cache;

    @Override
    public Shop getShop(ShopLocation location) {
        return cache.get(location);
    }

    @Override
    public void deleteShop(ShopLocation location, boolean breakBlock) {
        cache.remove(location);
        Block sign = Bukkit.getWorld(location.getWorldName())
                .getBlockAt(location.getX(), location.getY(), location.getZ());

        if (breakBlock) {
            sign.breakNaturally();
        } else {
            sign.setType(sign.getType());
        }
    }

    @Override
    public void updateSign(ShopLocation location) {
        Shop shop = getShop(location);
        shop.updateSign();
    }

    @Override
    public double getBuyPrice(ShopLocation location) {
        return cache.get(location).getBuyPrice();
    }

    @Override
    public double getSellPrice(ShopLocation location) {
        return cache.get(location).getSellPrice();
    }

    @Override
    public void setBuyPrice(ShopLocation location, double price) {
        cache.get(location).setBuyPrice(price);
        updateSign(location);
    }

    @Override
    public void setSellPrice(ShopLocation location, double price) {
        cache.get(location).setSellPrice(price);
        updateSign(location);
    }

    @Override
    public Collection<Shop> getAdminShops() {
        return cache.getAdminShops();
    }
}
