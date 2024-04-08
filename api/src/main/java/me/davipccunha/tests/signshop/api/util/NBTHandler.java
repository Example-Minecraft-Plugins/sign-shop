package me.davipccunha.tests.signshop.api.util;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class NBTHandler {
    public static ItemStack addNBT(ItemStack item, HashMap<String, String> keyValuesPairs) {

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        for (String key : keyValuesPairs.keySet()) {
            String value = keyValuesPairs.get(key);
            compound.setString(key, value);
        }

        nmsItem.setTag(compound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static String getNBT(ItemStack item, String key) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        return compound.hasKey(key) ? compound.getString(key) : null;
    }
}
