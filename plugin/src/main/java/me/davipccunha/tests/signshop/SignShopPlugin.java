package me.davipccunha.tests.signshop;

import lombok.Getter;
import me.davipccunha.tests.economy.api.EconomyAPI;
import me.davipccunha.tests.signshop.api.model.SignShopAPI;
import me.davipccunha.tests.signshop.cache.ShopCache;
import me.davipccunha.tests.signshop.listener.*;
import me.davipccunha.tests.signshop.provider.SignShopProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class SignShopPlugin extends JavaPlugin {
    private ShopCache shopCache;
    private EconomyAPI economyAPI;

    @Override
    public void onEnable() {
        this.init();
        getLogger().info("Sign Shop plugin loaded!");
        this.shopCache.removeGhostShops();
    }

    public void onDisable() {
        getLogger().info("Sign Shop plugin unloaded!");
    }

    private void init() {
        saveDefaultConfig();
        this.shopCache = new ShopCache(this.getConfig(), "shops");

        registerListeners(
                new SignChangeListener(this),
                new BlockBreakListener(this),
                new PlayerInteractListener(this),
                new EntityExplodeListener(this),
                new BlockBurnListener(this),
                new EntityChangeBlockListener(this),
                new BlockPlaceListener(this),
                new BlockPistonListener(this),
                new BlockFadeListener(this),
                new InventoryClickListener(this)
        );

        Bukkit.getServicesManager().register(SignShopAPI.class, new SignShopProvider(shopCache), this, ServicePriority.Normal);

        economyAPI = Bukkit.getServicesManager().load(EconomyAPI.class);
    }

    private void registerListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();

        for (Listener listener : listeners) pluginManager.registerEvents(listener, this);
    }
}
