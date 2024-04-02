package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

@RequiredArgsConstructor
public class BlockPlaceListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block == null) return;

        // This avoids ghost shops
        plugin.getShopCache().remove(block.getLocation());
    }
}
