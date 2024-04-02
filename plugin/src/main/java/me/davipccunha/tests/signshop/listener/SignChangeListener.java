package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.api.model.Shop;
import me.davipccunha.tests.signshop.api.model.ShopLocation;
import me.davipccunha.tests.signshop.api.model.ShopType;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;

@RequiredArgsConstructor
public class SignChangeListener implements Listener {
    private final SignShopPlugin plugin;

    private static final int INVENTORY_MAX_SIZE = 2304;

    @EventHandler
    private void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        final boolean isWallSign = ((Sign) block.getState().getData()).isWallSign();
        final boolean isChestBelow = block.getLocation().clone().add(0, -1, 0).getBlock().getState() instanceof Chest;


        final BlockFace signBack = ((Sign) block.getState().getData()).getAttachedFace();
        final boolean isAttachedToChest = block.getRelative(signBack).getState() instanceof Chest;
        final boolean isAdminShop = lines[0].equals("[AdminShop]") && player.isOp();

        if (!isWallSign && !(isChestBelow || isAdminShop)) return;
        if (isWallSign && !(isAttachedToChest || isAdminShop)) return;
        if (!(lines[0].equalsIgnoreCase("[Loja]") || isAdminShop)) return;

        if (lines[1].isEmpty() || (lines[2].isEmpty() && lines[3].isEmpty())) {
            cancelOperation(player, block, "§cPreencha todos os campos.");
            return;
        }

        this.createShop(player, block, lines, isAdminShop);
    }

    private void cancelOperation(Player player, Block block, String message) {
        player.sendMessage(message);
        block.breakNaturally();
    }

    private double[] getFullID(String line) {
        line = line.replaceAll(" ", "");
        String[] fullID = line.split(":");

        final int id = NumberUtils.toInt(fullID[0]);
        final short data = fullID.length > 1 ? NumberUtils.toShort(fullID[1]) : 0;

        return new double[] { id, data };
    }

    @SuppressWarnings("deprecation")
    private ItemStack getItem(int id, short data) {
        final Material material = Material.getMaterial(id);

        if (material == null || material == Material.AIR)
            return null;

        return new ItemStack(material, 1, data);
    }

    private double[] getSeparateInfo(String line) {
        line = line.replaceAll(" ", "");
        String[] info = line.split("/");
        int amount = info.length < 1 ? 0 : NumberUtils.toInt(info[0]);
        amount = amount < 0 ? 0 : Math.min(amount, INVENTORY_MAX_SIZE);

        final double price = info.length < 2 ? 0 : Math.max(0, NumberUtils.toDouble(info[1]));

        return new double[] { amount, price };
    }

    private void createShop(Player player, Block block, String[] lines, boolean isAdminShop) {
        final int id = (int) this.getFullID(lines[1])[0];
        final short data = (short) this.getFullID(lines[1])[1];

        ItemStack item = this.getItem(id, data);
        if (item == null) {
            cancelOperation(player, block, "§cNão existe um item com esse ID.");
            return;
        }

        double[] sellInfo = this.getSeparateInfo(lines[2]);

        final int sellAmount = (int) sellInfo[0];
        final double sellPrice = sellInfo[1];

        double[] buyInfo = this.getSeparateInfo(lines[3]);

        final int buyAmount = (int) buyInfo[0];
        final double buyPrice = buyInfo[1];

        if ((sellAmount != 0 && sellPrice == 0) || (buyAmount != 0 && buyPrice == 0)) {
            cancelOperation(player, block, "§cO preço não pode ser 0.");
            return;
        }

        if ((sellAmount == 0 && sellPrice != 0) || (buyAmount == 0 && buyPrice != 0)) {
            cancelOperation(player, block, "§cA quantidade não pode ser 0.");
            return;
        }

        if (sellAmount == 0 && buyAmount == 0) {
            cancelOperation(player, block, "§cA loja precisa vender ou comprar.");
            return;
        }

        String owner = player.getName();

        ShopLocation shopLocation = new ShopLocation(block.getLocation());
        Shop shop = isAdminShop ?
                new Shop(shopLocation, id, data, sellAmount, buyAmount, sellPrice, buyPrice, ShopType.ADMIN, null) :
                new Shop(shopLocation, id, data, sellAmount, buyAmount, sellPrice, buyPrice, ShopType.PLAYER, owner);

        Bukkit.getScheduler().runTaskLater(plugin, shop::updateSign, 2);

        plugin.getShopCache().add(shop);
    }
}
