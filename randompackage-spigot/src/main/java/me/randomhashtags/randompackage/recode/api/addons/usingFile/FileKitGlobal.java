package me.randomhashtags.randompackage.recode.api.addons.usingFile;

import me.randomhashtags.randompackage.recode.api.addons.RPKit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

public class FileKitGlobal extends RPKit {
    public static String heroicprefix;
    private ItemStack item;

    public FileKitGlobal(File f) {
        load(f);
        initilize();
    }
    public void initilize() { addKit("GLOBAL_" + getYamlName(), this); }

    public boolean isHeroic() { return yml.getBoolean("settings.heroic"); }
    public ItemStack getItem() {
        if(item == null) {
            item = api.d(yml, "gui settings");
            if(isHeroic()) {
                final ItemMeta m = item.getItemMeta();
                m.setDisplayName(heroicprefix.replace("{NAME}", m.hasDisplayName() ? ChatColor.stripColor(m.getDisplayName()) : item.getType().name()));
                item.setItemMeta(m);
            }
        }
        return item.clone();
    }

}
