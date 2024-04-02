package me.davipccunha.tests.signshop.api.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.davipccunha.tests.economy.api.EconomyAPI;
import me.davipccunha.tests.economy.api.EconomyType;
import org.bukkit.Bukkit;
import org.bukkit.block.*;
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
            sellLine.append("§aC: §0").append(sellAmount).append(" / ").append(String.format("%.2f", sellPrice)).append("¢");
        }

        if (validBuy) {
            buyLine.append("§cV: §0").append(buyAmount).append(" / ").append(String.format("%.2f", buyPrice)).append("¢");
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

    public void sell(EconomyAPI api, Player player, int amount) {
        if (amount <= 0) return;

        ItemStack itemStack = this.getItemStack().clone();
        itemStack.setAmount(amount);

        final double unitaryPrice = this.getSellPrice() / this.getSellAmount();
        final double finalPrice = amount * unitaryPrice;

        double balance = api.getBalance(player.getName(), EconomyType.COINS);

        if (balance < finalPrice) {
            player.sendMessage("§cVocê não tem coins suficientes.");
            return;
        }

        api.removeBalance(player.getName(), EconomyType.COINS, finalPrice);

        if (this.type == ShopType.PLAYER) {
            api.addBalance(this.owner, EconomyType.COINS, finalPrice);
            this.getInventory().removeItem(itemStack);
        }

        player.getInventory().addItem(itemStack);

        String message = "§aVocê comprou " + amount + " " + ItemName.valueOf(itemStack) + " por " + String.format("%.2f", finalPrice) + " coins.";
        player.sendMessage(message);
    }

    public void buy(EconomyAPI api, Player player, int amount) {
        if (amount <= 0) return;

        ItemStack itemStack = this.getItemStack().clone();
        itemStack.setAmount(amount);

        final double unitaryPrice = this.getBuyPrice() / this.getBuyAmount();
        final double finalPrice = amount * unitaryPrice;

        if (this.type == ShopType.PLAYER) {
            double balance = api.getBalance(this.owner, EconomyType.COINS);

            if (balance < finalPrice) {
                player.sendMessage("§cO dono da loja não tem coins suficientes.");
                return;
            }

            api.removeBalance(this.owner, EconomyType.COINS, finalPrice);
            this.getInventory().addItem(itemStack);
        }

        api.addBalance(player.getName(), EconomyType.COINS, finalPrice);

        player.getInventory().removeItem(itemStack);

        String message = "§aVocê vendeu " + amount + " " + ItemName.valueOf(itemStack) + " por " + String.format("%.2f", finalPrice) + " coins.";
        player.sendMessage(message);
    }

    public void breakSign() {
        Block sign = this.getShopSign();
        if (sign == null) return;
        this.getShopSign().breakNaturally();
    }

    private Block getShopSign() {
        Block block = Bukkit.getWorld(this.location.getWorldName()).getBlockAt(this.location.getX(), this.location.getY(), this.location.getZ());
        if (!(block.getState() instanceof Sign)) return null;
        return block;
    }
}
