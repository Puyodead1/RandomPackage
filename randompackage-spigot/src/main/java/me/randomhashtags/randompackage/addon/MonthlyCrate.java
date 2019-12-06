package me.randomhashtags.randompackage.addon;

import me.randomhashtags.randompackage.addon.util.Itemable;
import me.randomhashtags.randompackage.universal.UInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public interface MonthlyCrate extends Itemable {
    HashMap<Player, List<String>> revealedRegular = new HashMap<>(), revealedBonus = new HashMap<>();
    int getCategory();
    int getCategorySlot();
    String getGuiTitle();
    List<String> getRewards();
    List<String> getBonusRewards();
    ItemStack getBackground();
    ItemStack getRedeem();
    ItemStack getBonus1();
    ItemStack getBonus2();
    UInventory getRegular();
    UInventory getBonus();
    List<Integer> getRewardSlots();
    List<Integer> getBonusRewardSlots();
    ItemStack getRandomReward(Player player, List<String> excluding, boolean canRepeatRewards);
    ItemStack getRandomBonusReward(Player player, List<String> excluding, boolean canRepeatRewards);
}
