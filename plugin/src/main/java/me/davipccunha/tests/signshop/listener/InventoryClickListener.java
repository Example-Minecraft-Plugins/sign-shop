package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.api.model.Shop;
import me.davipccunha.tests.signshop.api.model.ShopLocation;
import me.davipccunha.tests.signshop.cache.ShopCache;
import me.davipccunha.tests.signshop.factory.ShopConfigGUIFactory;
import me.davipccunha.utils.item.NBTHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class InventoryClickListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory == null) return;
        if (event.getCurrentItem() == null) return;
        if (!inventory.getName().contains("Configuração da Loja")) return;

        event.setCancelled(true);

        if (!event.getCurrentItem().hasItemMeta()) return;
        if (!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        final Player player = (Player) event.getWhoClicked();

        if (player == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        final String action = NBTHandler.getNBT(clickedItem, "action");
        final String shopLocation = NBTHandler.getNBT(clickedItem, "shopLocation");

        if (action == null || shopLocation == null) return;

        final ShopCache cache = plugin.getShopCache();

        final ShopLocation location = ShopLocation.fromString(shopLocation);
        final Shop shop = cache.get(location);

        if (shop == null) return;

        switch (action) {
            case "togglePartialSelling":
                final boolean currentPartialSelling = shop.getShopConfig().isPartialSellingAllowed();
                shop.getShopConfig().setPartialSellingAllowed(!currentPartialSelling);
                cache.add(shop);

                player.sendMessage("§aVenda parcial " + (currentPartialSelling ? "§cdesativada" : "ativada") + " §acom sucesso.");

                break;
            case "toggleNotifications":
                final boolean currentNotifications = shop.getShopConfig().isNotificationsEnabled();
                shop.getShopConfig().setNotificationsEnabled(!currentNotifications);
                cache.add(shop);

                player.sendMessage("§aNotificações " + (currentNotifications ? "§cdesativadas" : "ativadas") + " §acom sucesso.");

                break;

            case "deleteShop":
                shop.breakSign();
                cache.remove(shop.getLocation());
                player.closeInventory();

                player.sendMessage("§aLoja deletada com sucesso.");

                return;
        }

        player.openInventory(ShopConfigGUIFactory.createShopConfigGUI(shop));
    }
}
