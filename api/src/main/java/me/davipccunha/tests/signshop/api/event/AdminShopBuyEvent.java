package me.davipccunha.tests.signshop.api.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.davipccunha.tests.signshop.api.model.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
@Getter
public class AdminShopBuyEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Shop shop;
    private final Player player;
    private final int amount;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
