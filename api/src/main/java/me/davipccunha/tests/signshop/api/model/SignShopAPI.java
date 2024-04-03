package me.davipccunha.tests.signshop.api.model;

import java.util.Collection;

public interface SignShopAPI {
    Shop getShop(ShopLocation location);

    void deleteShop(ShopLocation location);

    void setBuyPrice(ShopLocation location, double price);

    void setSellPrice(ShopLocation location, double price);

    Collection<Shop> getAdminShops();

    void updateAdminShops();
}
