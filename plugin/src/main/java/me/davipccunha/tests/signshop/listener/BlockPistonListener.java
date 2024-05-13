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

    @EventHandler(priority = EventPriority.MONITOR)
    private void onBlockExtendPiston(BlockPistonExtendEvent event) {
        // We check if the piston pushed a block to a ghost shop location
        final Block targetBlock = event.getBlock().getRelative(event.getDirection(), 2);
        plugin.getShopCache().remove(targetBlock.getLocation());

        // We check if the piston extending will break a shop
        List<Block> affectedBlocks = event.getBlocks();
        for (Block block : affectedBlocks) {
            IndirectShopDestroyer.indirectShopDelete(block, plugin.getShopCache());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onBlockRetractPiston(BlockPistonRetractEvent event) {
        // We check if the piston pulled a block to a ghost shop location
        if (!event.isSticky()) return;

        final Block targetBlock = event.getBlock().getRelative(event.getDirection().getOppositeFace());
        plugin.getShopCache().remove(targetBlock.getLocation());
    }
}
