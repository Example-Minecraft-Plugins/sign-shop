package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.api.model.ShopLocation;
import me.davipccunha.tests.signshop.cache.ShopCache;
import me.davipccunha.tests.signshop.listener.util.IndirectShopDestroyer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@RequiredArgsConstructor
public class BlockBreakListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (block == null) return;

        final ShopCache cache = plugin.getShopCache();
        final ShopLocation shopLocation = new ShopLocation(block.getLocation());

        if (cache.has(shopLocation)) event.setCancelled(true);

        if (!event.isCancelled()) {
            IndirectShopDestroyer.indirectShopDelete(block, plugin.getShopCache());
        }
    }
}
