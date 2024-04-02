package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.listener.util.IndirectShopDestroyer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

@RequiredArgsConstructor
public class BlockBurnListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (block == null) return;

        if (!event.isCancelled())
            IndirectShopDestroyer.indirectShopDelete(block, plugin.getShopCache());
    }
}
