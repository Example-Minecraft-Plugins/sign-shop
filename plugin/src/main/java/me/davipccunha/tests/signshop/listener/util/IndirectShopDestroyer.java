package me.davipccunha.tests.signshop.listener.util;

import me.davipccunha.tests.signshop.cache.ShopCache;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.List;

public class IndirectShopDestroyer {

    public static void indirectShopDelete(Block block, ShopCache cache) {
        for (Block attachedSign : getAttachedSigns(block))
            cache.remove(attachedSign.getLocation());
    }

    private static List<Block> getAttachedSigns(Block block) {
        final BlockFace[] DIRECTIONS = {
                BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST,
                BlockFace.UP,
        };

        List<Block> adjacentSigns = new ArrayList<>();

        for (BlockFace direction : DIRECTIONS) {
            Block adjacentBlock = block.getRelative(direction);
            if (adjacentBlock.getState() instanceof Sign) adjacentSigns.add(adjacentBlock);
        }

        return adjacentSigns;
    }
}
