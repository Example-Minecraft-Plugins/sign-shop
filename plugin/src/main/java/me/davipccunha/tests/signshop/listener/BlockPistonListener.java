package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.listener.util.IndirectShopDestroyer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

@RequiredArgsConstructor
public class BlockPistonListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockExtendPiston(BlockPistonExtendEvent event) {
        Block targetBlock = event.getBlock().getRelative(event.getDirection(), 2);
        plugin.getShopCache().remove(targetBlock.getLocation());

        List<Block> affectedBlocks = event.getBlocks();
        for (Block block : affectedBlocks) {
            if (!event.isCancelled())
                IndirectShopDestroyer.indirectShopDelete(block, plugin.getShopCache());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockRetractPiston(BlockPistonRetractEvent event) {
        if (!event.isSticky()) return;

        Block targetBlock = event.getBlock().getRelative(event.getDirection().getOppositeFace());
        plugin.getShopCache().remove(targetBlock.getLocation());
    }
}
