package me.davipccunha.tests.signshop.factory;

import me.davipccunha.tests.signshop.api.model.Shop;
import me.davipccunha.utils.inventory.InteractiveInventory;
import me.davipccunha.utils.item.ItemName;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ShopConfigGUIFactory {
    public static Inventory createShopConfigGUI(Shop shop) {
        Inventory inventory = Bukkit.createInventory(null, 4 * 9, "Configuração da Loja");

        String shopLocation = shop.getLocation().serialize();

        ItemStack sign = new ItemStack(Material.SIGN);
        ItemMeta signMeta = sign.getItemMeta();
        signMeta.setDisplayName("§r§eInformações da Loja");

        final List<String> shopInfoLore = Arrays.asList(
                "§7 * Dono: §f" + shop.getOwner(),
                "§7 * Item: §f" + ItemName.valueOf(shop.getItemStack()),
                String.format("§7 * A loja compra: §f%d por %.2f coins", shop.getBuyAmount(), shop.getBuyPrice()),
                String.format("§7 * A loja vende: §f%d por %.2f coins", shop.getSellAmount(), shop.getSellPrice())
        );

        signMeta.setLore(shopInfoLore);
        sign.setItemMeta(signMeta);

        final Map<String, String> partialSellingTags = Map.of(
            "action", "togglePartialSelling",
            "shopLocation", shopLocation
        );

        final ItemStack partialSelling = InteractiveInventory.createToggleItem(
                shop.getShopConfig().isPartialSellingAllowed(),
                partialSellingTags,
                "§r§eVenda Parcial",
                "§f * Permite a venda de quantidades inferiores à predefinida");

        final Map<String, String> notificationsTags = Map.of(
            "action", "toggleNotifications",
            "shopLocation", shopLocation
        );

        ItemStack notifications = InteractiveInventory.createToggleItem(
                shop.getShopConfig().isNotificationsEnabled(),
                notificationsTags,
                "§r§eNotificações",
                "§f * Envia mensagens de transações e estoque da loja");

        final Map<String, String> deleteShopTags = Map.of(
            "action", "deleteShop",
            "shopLocation", shopLocation
        );

        final List<String> deleteShopLore = List.of(
                "§f * Deleta esta loja"
        );

        ItemStack deleteShop = InteractiveInventory.createActionItem(
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
}
