package me.randomhashtags.randompackage.recode.api.addons;

import me.randomhashtags.randompackage.recode.RPAddon;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class Title extends RPAddon {
    public static ItemStack i;
    public static String titleChatFormat, titleTabFormat;

    public abstract String getTitle();
    public String getChatTitle() { return titleChatFormat.replace("{TITLE}", getTitle()); }
    public String getTabTitle() { return titleTabFormat.replace("{TITLE}", getTitle()); }

    public ItemStack getItem() {
        final String title = getTitle();
        final ItemStack item = i.clone();
        final ItemMeta itemMeta = item.getItemMeta();
        final List<String> a = new ArrayList<>();
        itemMeta.setDisplayName(itemMeta.getDisplayName().replace("{TITLE}", ChatColor.translateAlternateColorCodes('&', title)));
        for(String l : itemMeta.getLore()) {
            a.add(ChatColor.translateAlternateColorCodes('&', l.replace("{TITLE}", title)));
        }
        itemMeta.setLore(a);
        item.setItemMeta(itemMeta);
        return item;
    }

    public static Title valueOf(ItemStack is) {
        if(is != null && titles != null) {
            for(String s : titles.keySet()) {
                final Title T = titles.get(s);
                if(T.getItem().isSimilar(is)) {
                    return T;
                }
            }
        }
        return null;
    }
}
