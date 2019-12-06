package me.randomhashtags.randompackage.addon;

import me.randomhashtags.randompackage.addon.util.Itemable;
import me.randomhashtags.randompackage.universal.UInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public interface ServerCrate extends Itemable {
    int getRedeemableItems();
    String getDisplayRarity();
    List<Integer> getSelectableSlots();
    UInventory getInventory();
    List<String> getFormat();
    LinkedHashMap<String, Integer> getRevealChances();
    ItemStack getDisplay();
    ItemStack getOpenGui();
    ItemStack getSelected();
    ItemStack getRevealSlotRarity();
    HashMap<String, List<String>> getRewards();
    ItemStack getBackground();
    ItemStack getBackground2();
    ServerCrateFlare getFlare();
    ServerCrate getRandomRarity(boolean useChances);
    ItemStack getRandomReward(String rarity);
}
