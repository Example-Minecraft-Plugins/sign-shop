package me.davipccunha.tests.signshop.util.serializer;

import com.google.gson.Gson;
import me.davipccunha.tests.signshop.api.model.Shop;

public class ShopSerializer {

    private static final Gson GSON = new Gson();
    
    public static String serialize(Shop shop) {
        return GSON.toJson(shop);
    }

    public static Shop deserialize(String serialized) {
        return GSON.fromJson(serialized, Shop.class);
    }
}
