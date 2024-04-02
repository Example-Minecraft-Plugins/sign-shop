package me.davipccunha.tests.signshop.listener.util;

import me.davipccunha.tests.signshop.cache.ShopCache;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.List;

public class IndirectShopDestroyer {
    /* Known problem: There are too many specific situations where an AdminShop being indirectly destroyed is not detected.
    Example:
        Shop attached to a Sand Block that falls
        Shop attached to a TNT that is ignited
        Shop attached to Cake that is eaten

    Since it only happens with an AdminShop in very specific conditions, I might overlook some of these events.
        Problem caused: The shop is not removed from the cache.
        Workaround: When placing/moving a block, verify if its location is in the cache. If so, remove it.
    */
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
            if (adjacentBlock.getState() instanceof Sign) {
                BlockFace attachedFace = ((org.bukkit.material.Sign) adjacentBlock.getState().getData()).getAttachedFace();
                if (attachedFace == direction.getOppositeFace())
                    adjacentSigns.add(adjacentBlock);
            }
        }

        return adjacentSigns;
    }
}
