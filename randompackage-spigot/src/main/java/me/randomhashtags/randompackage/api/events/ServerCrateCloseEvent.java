package me.randomhashtags.randompackage.api.events;

import me.randomhashtags.randompackage.recode.api.events.AbstractEvent;
import me.randomhashtags.randompackage.recode.api.addons.usingFile.FileServerCrate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ServerCrateCloseEvent extends AbstractEvent {
    public final Player player;
    public final FileServerCrate crate;
    public final List<ItemStack> rewards;
    public ServerCrateCloseEvent(Player player, FileServerCrate crate, List<ItemStack> rewards) {
        this.player = player;
        this.crate = crate;
        this.rewards = rewards;
    }
}
