package me.davipccunha.tests.signshop.listener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.api.model.Shop;
import me.davipccunha.tests.signshop.api.model.ShopConfig;
import me.davipccunha.tests.signshop.api.model.ShopLocation;
import me.davipccunha.tests.signshop.api.model.ShopType;
import me.davipccunha.utils.cache.RedisConnector;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

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
        final boolean isAdminShop = lines[0].equals("[AdminShop]") && player.hasPermission("signshop.admin.create");

        if (!isWallSign && !(isChestBelow || isAdminShop)) return;
        if (isWallSign && !(isAttachedToChest || isAdminShop)) return;
        if (!(lines[0].equalsIgnoreCase("[Loja]") || isAdminShop)) return;

        if (lines[1].isEmpty()) {
            cancelOperation(player, block, "§cPreencha o ID do item.");
            return;
        }

        if (lines[2].isEmpty() && lines[3].isEmpty()) {
            cancelOperation(player, block, "§cA loja precisa comprar ou vender.");
            return;
        }

        this.createShop(player, block, lines, isAdminShop);
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

    private double getUnitaryBuyPrice(int id, short data) {
        final RedisConnector redisConnector = new RedisConnector();
        JsonParser parser = new JsonParser();

        try (Jedis jedis = redisConnector.getJedis()) {
            Pipeline pipeline = jedis.pipelined();
            Response<String> response = pipeline.hget("products", id + ":" + data);
            pipeline.sync();

            if (response == null || response.get() == null) return 0;

            JsonObject json = parser.parse(response.get()).getAsJsonObject();

            return json.get("buyPrice").getAsDouble();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void cancelOperation(Player player, Block block, String message) {
        player.sendMessage(message);
        block.breakNaturally();
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
        double sellPrice = sellInfo[1];

        double[] buyInfo = this.getSeparateInfo(lines[3]);

        final int buyAmount = (int) buyInfo[0];
        double buyPrice = buyInfo[1];

        if (isAdminShop) {
            double unitaryBuyPrice = this.getUnitaryBuyPrice(id, data);
            if (unitaryBuyPrice != 0) {
                if (buyPrice == 0 && buyAmount != 0)  buyPrice = unitaryBuyPrice * buyAmount;
                if (sellPrice == 0 && sellAmount != 0) sellPrice = buyPrice * 1.3;
            }
        }

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

        final String owner = player.getName();

        final ShopLocation shopLocation = new ShopLocation(block.getLocation());
        final ShopConfig defaultConfig = new ShopConfig(true, false);

        final Shop shop = isAdminShop ?
                new Shop(shopLocation, id, data, sellAmount, buyAmount, sellPrice, buyPrice, ShopType.ADMIN, null, defaultConfig) :
                new Shop(shopLocation, id, data, sellAmount, buyAmount, sellPrice, buyPrice, ShopType.PLAYER, owner, defaultConfig);

        Bukkit.getScheduler().runTaskLater(plugin, shop::updateSign, 2);

        plugin.getShopCache().add(shop);
    }
}
