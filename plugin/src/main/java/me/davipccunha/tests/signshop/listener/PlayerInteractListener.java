package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.api.ShopType;
import me.davipccunha.tests.signshop.cache.ShopCache;
import me.davipccunha.tests.signshop.api.Shop;
import me.davipccunha.tests.signshop.api.ShopLocation;
import me.davipccunha.tests.signshop.util.InventoryUtil;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@RequiredArgsConstructor
public class PlayerInteractListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Sign)) return;

        final ShopCache cache = plugin.getShopCache();

        Shop shop = cache.get(new ShopLocation(block.getLocation()));
        if (shop == null) return;

        Player player = event.getPlayer();

        if (action == Action.LEFT_CLICK_BLOCK) {
            if (shop.getBuyAmount() <= 0 || shop.getBuyPrice() <= 0) {
                player.sendMessage("§cEsta loja não compra itens.");
                return;
            }

            final int playerAmount = InventoryUtil.getTotalAmount(player.getInventory(), shop.getItemStack());

            final int shopAmount = shop.getBuyAmount();
            int realAmount = Math.min(playerAmount, shopAmount);

            if (playerAmount <= 0) {
                player.sendMessage("§cVocê não tem itens para vender.");
                return;
            }

            if (shop.getType() == ShopType.PLAYER) {
//                if (player.getName().equals(shop.getOwner())) {
//                    shop.breakShop();
//                    cache.remove(shop.getLocation());
//                    player.sendMessage("§cLoja deletada com sucesso.");
//                    return;
//                }

                final int missingAmount = InventoryUtil.getMissingAmount((shop.getInventory()), shop.getItemStack());

                if (missingAmount <= 0) {
                    player.sendMessage("§cA loja não tem espaço livre.");

                    return;
                }

                realAmount = Math.min(realAmount, missingAmount);
            }

            shop.buy(plugin.getEconomyAPI(), player, realAmount);

        } else {
            if (shop.getSellAmount() <= 0 || shop.getSellPrice() <= 0) {
                player.sendMessage("§cEsta loja não vende itens.");
                return;
            }

            final int missingAmount = InventoryUtil.getMissingAmount(player.getInventory(), shop.getItemStack());
            if (missingAmount <= 0) {
                player.sendMessage("§cVocê não tem espaço livre.");
                return;
            }

            final int shopAmount = shop.getSellAmount();

            int realAmount = Math.min(missingAmount, shopAmount);

            if (shop.getType() == ShopType.PLAYER) {
                // TODO: Open a shop configuration GUI when the owner interacts -> Notification to the owner and allow partial selling

                final int chestAmount = InventoryUtil.getTotalAmount(shop.getInventory(), shop.getItemStack());
                if (chestAmount <= 0) {
                    player.sendMessage("§cA loja não tem itens para vender.");
                    return;
                }

                realAmount = Math.min(realAmount, chestAmount);
            }

            shop.sell(plugin.getEconomyAPI(), player, realAmount);
        }
    }
}
