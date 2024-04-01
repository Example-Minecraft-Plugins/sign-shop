package me.davipccunha.tests.signshop.api;

import java.util.Collection;

public interface SignShopAPI {
    Shop getShop(ShopLocation location);

    void deleteShop(ShopLocation location, boolean breakBlock);

    void updateSign(ShopLocation location);

    double getBuyPrice(ShopLocation location);

    double getSellPrice(ShopLocation location);

    void setBuyPrice(ShopLocation location, double price);

    void setSellPrice(ShopLocation location, double price);

    Collection<Shop> getAdminShops();
}
