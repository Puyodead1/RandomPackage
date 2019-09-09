package me.randomhashtags.randompackage.dev;

import me.randomhashtags.randompackage.addon.util.Attributable;
import me.randomhashtags.randompackage.addon.util.Itemable;
import me.randomhashtags.randompackage.addon.util.Scheduleable;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;

public interface Dungeon extends Attributable, Itemable, Scheduleable {
    int getSlot();
    ItemStack getKey();
    ItemStack getKeyLocked();
    ItemStack getPortal();
    Recipe getPortalRecipe();
    Location getTeleportLocation();
    List<String> getAllowedCommands();
    List<DungeonBoss> getBosses();

    ItemStack getLootbag();
    List<String> getLootbagRewards();

    long getFastestCompletion();
}
