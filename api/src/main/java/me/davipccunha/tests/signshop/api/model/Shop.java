package me.davipccunha.tests.signshop.api.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.davipccunha.tests.signshop.api.event.AdminShopBuyEvent;
import me.davipccunha.tests.signshop.api.util.ItemName;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
@Setter
public class Shop {
    private final ShopLocation location;
    private final int itemID;
    private final short itemData;
    private final int sellAmount, buyAmount;

    @Setter(AccessLevel.NONE)
    private double sellPrice, buyPrice;

    private final ShopType type;
    private final String owner;
    private ShopConfig shopConfig;

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
        this.updateSign();
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
        this.updateSign();
    }

    public boolean isAdminShop() {
        return this.type == ShopType.ADMIN;
    }

    public double getUnitaryBuyPrice() {
        return this.buyPrice / this.buyAmount;
    }

    @SuppressWarnings("deprecation")
    public ItemStack getItemStack() {
        return new ItemStack(itemID, 1, itemData);
    }

    public Inventory getInventory() {
        if (this.type == ShopType.ADMIN) return null;

        Block shopSign = this.getShopSign();
        if (shopSign == null) return null;

        final Block belowBlock = shopSign.getLocation().clone().add(0, -1, 0).getBlock();
        final Block behindBlock = shopSign.getRelative(((org.bukkit.material.Sign) shopSign.getState().getData()).getAttachedFace());

        final boolean isWallSign = ((org.bukkit.material.Sign) shopSign.getState().getData()).isWallSign();
        final boolean isChestBelow = belowBlock.getState() instanceof Chest;
        final boolean isAttachedToChest = behindBlock.getState() instanceof Chest;

        if (!isWallSign && isChestBelow)
            return ((Chest) belowBlock.getState()).getInventory();
        if (isWallSign && isAttachedToChest)
            return ((Chest) behindBlock.getState()).getInventory();

        return null;
    }

    public void updateSign() {
        BlockState signState = Bukkit.getWorld(location.getWorldName())
                .getBlockAt(location.getX(), location.getY(), location.getZ())
                .getState();

        if (!(signState instanceof org.bukkit.block.Sign)) return;

        StringBuilder sellLine = new StringBuilder();
        StringBuilder buyLine = new StringBuilder();

        final boolean validSell = this.sellAmount > 0 && this.sellPrice > 0;
        final boolean validBuy = this.buyAmount > 0 && this.buyPrice > 0;

        if (!validSell && !validBuy) return;

        if (validSell) {
            sellLine.append("§aC: §0").append(sellAmount).append(" / ").append(String.format("%.2f", sellPrice));
        }

        if (validBuy) {
            buyLine.append("§cV: §0").append(buyAmount).append(" / ").append(String.format("%.2f", buyPrice));
        }

        Sign sign = (Sign) signState;

        String owner = this.getType() == ShopType.PLAYER ? this.getOwner() : "Pluncky";
        String itemName = ItemName.valueOf(this.getItemStack()).toString();

        sign.setLine(0, owner);
        sign.setLine(1, itemName.substring(0, Math.min(itemName.length(), 15)));
        sign.setLine(2, sellLine.toString());
        sign.setLine(3, buyLine.toString());

        sign.update(true, true);
    }

    public void sell(Player player, int amount) {
        if (amount <= 0) return;

        ItemStack itemStack = this.getItemStack().clone();
        itemStack.setAmount(amount);

        final double unitaryPrice = this.getSellPrice() / this.getSellAmount();
        final double finalPrice = amount * unitaryPrice;

        if (this.type == ShopType.PLAYER)
            this.getInventory().removeItem(itemStack);

        player.getInventory().addItem(itemStack);

        String message = String.format("§aVocê comprou %d %s por %.2f coins.",
                amount, ItemName.valueOf(itemStack), finalPrice);

        player.sendMessage(message);
    }

    public void buy(Player player, int amount) {
        if (amount <= 0) return;

        ItemStack itemStack = this.getItemStack().clone();
        itemStack.setAmount(amount);

        final double unitaryPrice = this.getBuyPrice() / this.getBuyAmount();
        final double finalPrice = amount * unitaryPrice;

        if (this.type == ShopType.PLAYER)
            this.getInventory().addItem(itemStack);

        // We must reset the ItemStack's amount because Inventory#addItem() modifies it
        itemStack.setAmount(amount);
        player.getInventory().removeItem(itemStack);

        String message = String.format("§aVocê vendeu %d %s por %.2f coins.",
                amount, ItemName.valueOf(itemStack), finalPrice);

        player.sendMessage(message);

        if (this.isAdminShop()) {
            AdminShopBuyEvent adminShopBuyEvent = new AdminShopBuyEvent(this, player, amount);
            Bukkit.getPluginManager().callEvent(adminShopBuyEvent);
        }
    }

    public void breakSign() {
        Block sign = this.getShopSign();
        if (sign == null) return;
        this.getShopSign().breakNaturally();
    }

    public Block getShopSign() {
        Block block = Bukkit.getWorld(this.location.getWorldName()).getBlockAt(this.location.getX(), this.location.getY(), this.location.getZ());
        if (!(block.getState() instanceof Sign)) return null;
        return block;
    }

    public void sendNotification(String message) {
        Player player = Bukkit.getPlayer(this.owner);
        if (player != null && this.shopConfig.isNotificationsEnabled())
            player.sendMessage(message);
    }
}
