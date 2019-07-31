package me.randomhashtags.randompackage.addons;

import me.randomhashtags.randompackage.addons.utils.Identifyable;
import me.randomhashtags.randompackage.api.CustomEnchants;
import me.randomhashtags.randompackage.utils.RPAddon;
import me.randomhashtags.randompackage.utils.universal.UMaterial;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class CustomEnchant extends RPAddon implements Identifyable {
    public abstract boolean isEnabled();
    public abstract String getName();
    public abstract List<String> getLore();
    public abstract int getMaxLevel();
    public abstract List<String> getAppliesTo();
    public abstract String getRequiredEnchant();
    public abstract int[] getAlchemist();
    public abstract int getAlchemistUpgradeCost(int level);
    public abstract int[] getTinkerer();
    public abstract int getTinkererValue(int level);
    public abstract String getEnchantProcValue();
    public abstract List<String> getAttributes();

    public static CustomEnchant valueOf(String string) { return valueOf(string, false); }
    public static CustomEnchant valueOf(String string, boolean checkDisabledEnchants) {
        if(string != null) {
            final String s = ChatColor.stripColor(string);
            if(enabled != null) {
                for(CustomEnchant ce : enabled.values()) {
                    if(s.startsWith(ce.getIdentifier()) || s.startsWith(ChatColor.stripColor(ce.getName())))
                        return ce;
                }
            }
            if(checkDisabledEnchants && disabled != null) {
                for(CustomEnchant ce : disabled.values()) {
                    if(s.startsWith(ce.getIdentifier()) || s.startsWith(ChatColor.stripColor(ce.getName())))
                        return ce;
                }
            }
        }
        return null;
    }
    public static CustomEnchant valueOf(ItemStack is) {
        if(is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()) {
            final CustomEnchant e = valueOf(is.getItemMeta().getDisplayName());
            final EnchantRarity r = CustomEnchants.getCustomEnchants().valueOfEnchantRarity(e);
            return e != null && UMaterial.match(is).equals(UMaterial.match(r.getRevealedItem())) ? e : null;
        }
        return null;
    }
}
