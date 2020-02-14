package me.randomhashtags.randompackage.addon;

import me.randomhashtags.randompackage.addon.util.Itemable;
import me.randomhashtags.randompackage.universal.UInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.randomhashtags.randompackage.RandomPackageAPI.API;

public interface MonthlyCrate extends Itemable, GivedpItemable {
    HashMap<Player, List<String>> revealedRegular = new HashMap<>(), revealedBonus = new HashMap<>();

    default String[] getGivedpItemIdentifiers() {
        return new String[] { "monthlycrate" };
    }
    default ItemStack valueOfInput(String originalInput, String lowercaseInput) {
        final MonthlyCrate crate = getMonthlyCrate(originalInput.split(":")[1]);
        final ItemStack target = crate != null ? crate.getItem() : null;
        return target != null ? target : AIR;
    }

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
    default List<ItemStack> getAllRewards() {
        final List<ItemStack> items = new ArrayList<>();
        for(String reward : getRewards()) {
            final ItemStack target = API.createItemStack(null, reward);
            if(target != null && !target.getType().equals(Material.AIR)) {
                items.add(target);
            }
        }
        for(String reward : getBonusRewards()) {
            final ItemStack target = API.createItemStack(null, reward);
            if(target != null && !target.getType().equals(Material.AIR)) {
                items.add(target);
            }
        }
        return items;
    }
}
