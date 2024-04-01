package me.davipccunha.tests.signshop.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryUtil {
    public static List<Integer> getEmptySlots(Inventory inventory) {
        List<Integer> emptySlots = new ArrayList<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) emptySlots.add(i);
        }

        return emptySlots;
    }

    public static int getMissingAmount(Inventory inventory, ItemStack itemStack) {
        if (itemStack == null) return 0;

        int incompleteStacksAmount = Arrays.stream(inventory.getContents())
                .filter(item -> item != null
                        && item.isSimilar(itemStack)
                        && item.getAmount() < itemStack.getMaxStackSize())
                .mapToInt(item -> itemStack.getMaxStackSize() - item.getAmount())
                .sum();

        int emptySlotsAmount = getEmptySlots(inventory).size() * itemStack.getMaxStackSize();

        return incompleteStacksAmount + emptySlotsAmount;
    }

    public static int getTotalAmount(Inventory inventory, ItemStack itemStack) {
        if (itemStack == null) return 0;

        return Arrays.stream(inventory.getContents())
                .filter(item -> item != null && item.isSimilar(itemStack))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }
}
