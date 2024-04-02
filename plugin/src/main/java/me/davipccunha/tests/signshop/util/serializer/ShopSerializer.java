package me.davipccunha.tests.signshop.util.serializer;

import com.google.gson.Gson;
import me.davipccunha.tests.signshop.api.model.Shop;

public class ShopSerializer {
    public static String serialize(Shop shop) {
        Gson gson = new Gson();
        return gson.toJson(shop);
    }

    public static Shop deserialize(String serialized) {
        Gson gson = new Gson();
        return gson.fromJson(serialized, Shop.class);
    }
}
