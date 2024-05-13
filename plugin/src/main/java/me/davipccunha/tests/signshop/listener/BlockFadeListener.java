package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.listener.util.IndirectShopDestroyer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

@RequiredArgsConstructor
public class BlockFadeListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onBlockFade(BlockFadeEvent event) {
        final Block block = event.getBlock();
        if (block == null) return;

        IndirectShopDestroyer.indirectShopDelete(block, plugin.getShopCache());
    }
}
