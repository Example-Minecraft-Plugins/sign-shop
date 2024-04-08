package me.davipccunha.tests.signshop.factory;

import me.davipccunha.tests.signshop.api.model.Shop;
import me.davipccunha.tests.signshop.api.util.ItemName;
import me.davipccunha.tests.signshop.api.util.NBTHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ShopConfigGUIFactory {
    public static Inventory createShopConfigGUI(Shop shop) {
        Inventory inventory = Bukkit.createInventory(null, 4 * 9, "Configuração da Loja");

        String shopLocation = shop.getLocation().serialize();

        ItemStack sign = new ItemStack(Material.SIGN);
        ItemMeta signMeta = sign.getItemMeta();
        signMeta.setDisplayName("§r§eInformações da Loja");

        final List<String> shopInfolore = Arrays.asList(
                "§7 * Dono: §f" + shop.getOwner(),
                "§7 * Item: §f" + ItemName.valueOf(shop.getItemStack()),
                String.format("§7 * A loja compra: §f%d por %.2f coins", shop.getBuyAmount(), shop.getBuyPrice()),
                String.format("§7 * A loja vende: §f%d por %.2f coins", shop.getSellAmount(), shop.getSellPrice())
        );

        signMeta.setLore(shopInfolore);
        sign.setItemMeta(signMeta);

        ItemStack partialSelling = createConfigItem(
                shop.getShopConfig().isPartialSellingAllowed(),
                "togglePartialSelling",
                shopLocation,
                "§r§eVenda Parcial",
                "§f * Permite a venda de quantidades inferiores à predefinida");

        
        ItemStack notifications = createConfigItem(
                shop.getShopConfig().isNotificationsEnabled(),
                "toggleNotifications",
                shopLocation,
                "§r§eNotificações",
                "§f * Envia mensagens de transações e estoque da loja");

        final HashMap<String, String> deleteShopTags = new HashMap<>() {{
            put("action", "deleteShop");
            put("shopLocation", shopLocation);
        }};

        final List<String> deleteShopLore = List.of(
                "§f * Deleta esta loja"
        );

        ItemStack deleteShop = createActionItem(
                new ItemStack(Material.BARRIER),
                deleteShopTags,
                "§r§cDeletar Loja",
                deleteShopLore);

        inventory.setItem(4, sign);
        inventory.setItem(20, partialSelling);
        inventory.setItem(22, notifications);
        inventory.setItem(24, deleteShop);
        
        return inventory;
    }

    private static ItemStack createConfigItem(boolean config, String action, String shopLocation, String name, String description) {
        ItemStack wool = new ItemStack(Material.WOOL, 1, (short) (config ? 5 : 14));

        final HashMap<String, String> tags = new HashMap<>() {{
           put("action", action);
           put("shopLocation", shopLocation);
        }};

        final List<String> lore = Arrays.asList(
                description,
                "§7 Clique para " + (config ? "§cDesativar" : "§aAtivar")
        );

        return createActionItem(wool, tags, name, lore);
    }

    private static ItemStack createActionItem(ItemStack item, HashMap<String, String> NBTTags, String name, List<String> lore) {

        ItemStack actionItem = NBTHandler.addNBT(item, NBTTags);

        ItemMeta actionItemMeta = actionItem.getItemMeta();
        actionItemMeta.setDisplayName(name);

        actionItemMeta.setLore(lore);
        actionItem.setItemMeta(actionItemMeta);

        return actionItem;
    }
}
