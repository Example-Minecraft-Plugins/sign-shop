package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.economy.api.EconomyAPI;
import me.davipccunha.tests.economy.api.EconomyType;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.api.model.Shop;
import me.davipccunha.tests.signshop.api.model.ShopLocation;
import me.davipccunha.tests.signshop.api.model.ShopType;
import me.davipccunha.tests.signshop.cache.ShopCache;
import me.davipccunha.tests.signshop.factory.ShopConfigGUIFactory;
import me.davipccunha.utils.inventory.InventoryUtil;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
public class PlayerInteractListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler(priority = EventPriority.HIGH)
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
            if (shop.getType() == ShopType.PLAYER && player.getName().equals(shop.getOwner())) {
                shop.breakSign();
                cache.remove(shop.getLocation());
                player.sendMessage("§cLoja deletada com sucesso.");
                return;
                }

            if (shop.getBuyAmount() <= 0 || shop.getBuyPrice() <= 0) {
                player.sendMessage("§cEsta loja não compra itens.");
                return;
            }

            int playerAmount = InventoryUtil.getTotalAmount(player.getInventory(), shop.getItemStack());

            final int shopAmount = shop.getBuyAmount();
            final boolean ignoreShopLimit = player.isSneaking() && shop.getShopConfig().isPartialSellingAllowed();
            final boolean sellMultiple = player.isSneaking() && !shop.getShopConfig().isPartialSellingAllowed();

            // If the player is sneaking, and the shop does not allow partial selling,
            // we sell the maximum amount the player has that is a multiple of the shop buyAmount
            if (sellMultiple)
                playerAmount = (playerAmount / shopAmount) * shopAmount;

            // If the player is sneaking, the final amount is the player's amount (rounded if shop does not allow partial selling)
            // If the player is not sneaking, the final amount is the minimum between the player's amount and the shop's buyAmount
            int realAmount = (ignoreShopLimit || sellMultiple) ? playerAmount : Math.min(playerAmount, shopAmount);

            if ((playerAmount < shopAmount) && !shop.getShopConfig().isPartialSellingAllowed()) {
                player.sendMessage("§cVocê não tem a quantidade mínima de itens para vender.");
                return;
            }

            if (playerAmount <= 0) {
                player.sendMessage("§cVocê não tem itens para vender.");
                return;
            }

            if (shop.getType() == ShopType.PLAYER) {
                final int missingAmount = InventoryUtil.getMissingAmount((shop.getInventory()), shop.getItemStack());

                if (missingAmount <= 0) {
                    player.sendMessage("§cA loja não tem espaço livre.");
                    shop.sendNotification(String.format("§cO baú da sua loja em §f%s §cestá cheia.", shop.getLocation().toString()));

                    return;
                }

                realAmount = Math.min(realAmount, missingAmount);
            }

            this.playerSells(shop, player, realAmount, plugin.getEconomyAPI());

        } else {
            if (shop.getType() == ShopType.PLAYER && player.getName().equals(shop.getOwner())) {
                Inventory shopConfigGUI = ShopConfigGUIFactory.createShopConfigGUI(shop);
                player.openInventory(shopConfigGUI);

                return;
            }

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

                final int chestAmount = InventoryUtil.getTotalAmount(shop.getInventory(), shop.getItemStack());
                if (chestAmount <= 0) {
                    player.sendMessage("§cA loja não tem itens para vender.");
                    shop.sendNotification(String.format("§cA loja em §f%s §cnão tem itens para vender.", shop.getLocation().toString()));

                    return;
                }

                realAmount = Math.min(realAmount, chestAmount);
            }

            this.playerBuys(shop, player, realAmount, plugin.getEconomyAPI());
        }
    }

    private void playerSells(Shop shop, Player player, int amount, EconomyAPI economyAPI) {
        if (amount <= 0) return;

        final double unitaryPrice = shop.getBuyPrice() / shop.getBuyAmount();
        final double finalPrice = amount * unitaryPrice;

        if (shop.getType() == ShopType.PLAYER) {
            final double balance = economyAPI.getBalance(shop.getOwner(), EconomyType.COINS);

            if (balance < finalPrice) {
                player.sendMessage("§cO dono da loja não tem coins suficientes.");
                shop.sendNotification("§cTentaram comprar itens de sua loja mas você não tem coins suficientes.");

                return;
            }

            economyAPI.removeBalance(shop.getOwner(), EconomyType.COINS, finalPrice);
        }

        economyAPI.addBalance(player.getName(), EconomyType.COINS, finalPrice);

        shop.buy(player, amount);
    }

    private void playerBuys(Shop shop, Player player, int amount, EconomyAPI economyAPI) {
        if (amount <= 0) return;

        final double balance = economyAPI.getBalance(player.getName(), EconomyType.COINS);
        final double unitaryPrice = shop.getSellPrice() / shop.getSellAmount();
        final double finalPrice = amount * unitaryPrice;

        if (balance < finalPrice) {
            player.sendMessage("§cVocê não tem coins suficientes.");
            return;
        }

        if (shop.getType() == ShopType.PLAYER)
            economyAPI.addBalance(shop.getOwner(), EconomyType.COINS, finalPrice);

        economyAPI.removeBalance(player.getName(), EconomyType.COINS, finalPrice);

        shop.sell(player, amount);
    }
}
