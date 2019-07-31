package me.randomhashtags.randompackage.addons;

import me.randomhashtags.randompackage.addons.utils.Itemable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public interface RarityGem extends Itemable {
    ItemStack getItem(int souls);
    List<EnchantRarity> getWorksFor();
    List<String> getSplitMsg();
    long getTimeBetweenSameKills();
    HashMap<Integer, String> getColors();
    List<String> getToggleOnMsg();
    List<String> getToggleOffInteractMsg();
    List<String> getToggleOffDroppedMsg();
    List<String> getToggleOffMovedMsg();
    List<String> getToggleOffRanOutMsg();
    String getColors(int soulsCollected);
}
