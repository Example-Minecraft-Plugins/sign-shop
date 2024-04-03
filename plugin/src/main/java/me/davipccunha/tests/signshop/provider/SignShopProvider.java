package me.davipccunha.tests.signshop.provider;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.api.model.Shop;
import me.davipccunha.tests.signshop.api.model.ShopLocation;
import me.davipccunha.tests.signshop.api.model.SignShopAPI;
import me.davipccunha.tests.signshop.cache.ShopCache;

import java.util.Collection;

@RequiredArgsConstructor
public class SignShopProvider implements SignShopAPI {
    private final ShopCache cache;

    @Override
    public Shop getShop(ShopLocation location) {
        return cache.get(location);
    }

    @Override
    public void deleteShop(ShopLocation location) {
        Shop shop = cache.get(location);

        if (shop == null) return;

        shop.breakSign();
        cache.remove(location);
    }

    @Override
    public void setBuyPrice(ShopLocation location, double price) {
        Shop shop = cache.get(location);
        if (shop == null) return;

        shop.setBuyPrice(price);
        shop.updateSign();
        cache.add(shop);
    }

    @Override
    public void setSellPrice(ShopLocation location, double price) {
        Shop shop = cache.get(location);
        if (shop == null) return;

        shop.setSellPrice(price);
        shop.updateSign();
        cache.add(shop);
    }

    @Override
    public Collection<Shop> getAdminShops() {
        return cache.getAdminShops();
    }

    @Override
    public void updateAdminShops() {
        this.getAdminShops().forEach(Shop::updateSign);
    }
}
