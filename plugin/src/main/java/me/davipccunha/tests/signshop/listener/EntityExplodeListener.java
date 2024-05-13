package me.davipccunha.tests.signshop.listener;

import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.SignShopPlugin;
import me.davipccunha.tests.signshop.listener.util.IndirectShopDestroyer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

@RequiredArgsConstructor
public class EntityExplodeListener implements Listener {
    private final SignShopPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onBlockExplode(EntityExplodeEvent event) {
        List<Block> explodedBlocks = event.blockList();

        for (Block block : explodedBlocks)
            IndirectShopDestroyer.indirectShopDelete(block, plugin.getShopCache());
    }
}
