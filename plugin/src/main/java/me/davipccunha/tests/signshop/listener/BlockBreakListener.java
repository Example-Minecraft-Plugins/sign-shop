package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.api.ShopLocation;
import me.davipccunha.tests.signshop.cache.ShopCache;
import me.davipccunha.tests.signshop.listener.util.IndirectShopDestroyer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@RequiredArgsConstructor
public class BlockBreakListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block == null) return;

        IndirectShopDestroyer.indirectShopDelete(block, plugin.getShopCache());

        if (!(block.getState() instanceof Sign)) return;

        final ShopCache cache = plugin.getShopCache();

        ShopLocation shopLocation = new ShopLocation(block.getLocation());

        if (cache.has(shopLocation)) event.setCancelled(true);
    }
}
