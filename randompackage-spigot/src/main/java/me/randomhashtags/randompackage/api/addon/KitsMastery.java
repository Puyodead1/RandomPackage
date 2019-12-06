package me.randomhashtags.randompackage.api.addon;

import me.randomhashtags.randompackage.addon.*;
import me.randomhashtags.randompackage.util.RPPlayer;
import me.randomhashtags.randompackage.addon.file.FileKitMastery;
import me.randomhashtags.randompackage.universal.UInventory;
import me.randomhashtags.randompackage.universal.UMaterial;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class KitsMastery extends Kits {
    private static KitsMastery instance;
    public static KitsMastery getKitsMastery() {
        if(instance == null) instance = new KitsMastery();
        return instance;
    }

    private UInventory gui, preview;
    private ItemStack background, cooldown;
    private List<String> permissionsUnlocked, permissionsLocked, permissionsPreview;

    public String getIdentifier() { return "KITS_MASTERY"; }
    public boolean executeCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) { return false; }
    public Class<? extends CustomKit> getCustomKit() { return CustomKitMastery.class; }
    public String getPath() { return "mkits"; }

    public void load() {
        loadKitUtils();
        final long started = System.currentTimeMillis();
        if(!otherdata.getBoolean("saved default mkits")) {
            final String[] v = new String[] {"DEATH_KNIGHT", "GHOST", "NECROMANCER"};
            for(String s : v) save("kits", "MKIT_" + s + ".yml");
            otherdata.set("saved default mkits", true);
            saveOtherData();
        }

        gui = new UInventory(null, config.getInt("mkits.gui.size"), colorize(config.getString("mkits.gui.title")));
        permissionsUnlocked = colorizeListString(config.getStringList("mkits.permissions.unlocked"));
        permissionsLocked = colorizeListString(config.getStringList("mkits.permissions.locked.lore"));
        permissionsPreview = colorizeListString(config.getStringList("mkits.permissions.preview"));
        preview = new UInventory(null, 54, colorize(config.getString("mkits.items.preview.title")));
        background = d(config, "mkits.gui.background");

        final List<ItemStack> gems = new ArrayList<>();
        final Inventory mi = gui.getInventory();
        final File folder = new File(DATA_FOLDER + SEPARATOR + "kits");
        int loaded = 0;
        if(folder.exists()) {
            for(File f : folder.listFiles()) {
                if(f.getName().startsWith("MKIT_")) {
                    final FileKitMastery m = new FileKitMastery(f);
                    mi.setItem(m.getSlot(), m.getItem());
                    gems.add(m.getRedeem());
                    loaded++;
                }
            }
        }
        addGivedpCategory(gems, UMaterial.DIAMOND, "Mkit Gems", "Givedp: Mkit Gems");
        for(int i = 0; i < gui.getSize(); i++) {
            if(mi.getItem(i) == null)
                mi.setItem(i, background);
        }
        sendConsoleMessage("&6[RandomPackage] &aLoaded " + loaded + " Mastery Kits &e(took " + (System.currentTimeMillis()-started) + "ms)");
    }
    public void unload() {
        if(kits != null) {
            for(CustomKit k : new ArrayList<>(kits.values())) {
                if(k instanceof CustomKitMastery) {
                    kits.remove(k.getIdentifier());
                }
            }
        }
        unloadKitUtils();
    }
    public boolean usesTiers() { return false; }
    public TreeMap<Integer, Float> getCustomEnchantLevelMultipliers() { return null; }
    public UInventory getPreview() { return preview; }
    public ItemStack getOmniGem() { return null; }
    public List<String> getNotInWarzoneMsg() { return null; }
    public List<String> getAlreadyHaveMaxTierMsg() { return null; }
    public List<String> getRedeemFallenHeroGemMsg() { return null; }
    public List<String> getUpgradeMsg() { return null; }
    public List<String> getResetTargetDoesntExist() { return null; }
    public List<String> getResetSuccess() { return null; }
    public ItemStack getPreviewBackground() { return null; }
    public ItemStack getCooldown() { return cooldown.clone(); }
    public List<String> getPermissionsUnlocked() { return permissionsUnlocked; }
    public List<String> getPermissionsLocked() { return permissionsLocked; }
    public List<String> getPermissionsPreview() { return permissionsPreview; }

    public void view(Player player) {
        player.closeInventory();
        player.openInventory(Bukkit.createInventory(player, gui.getSize(), gui.getTitle()));
        final Inventory top = player.getOpenInventory().getTopInventory();
        top.setContents(gui.getInventory().getContents());
        player.updateInventory();
        for(int i = 0; i < top.getSize(); i++) {
            final CustomKit m = valueOfCustomKit(i, CustomKitMastery.class);
            if(m != null) {
                item = top.getItem(i); itemMeta = item.getItemMeta(); lore.clear();
                if(itemMeta.hasLore()) {
                    for(String s : itemMeta.getLore()) {
                        if(s.contains("{") && s.contains("}")) {
                            final String t = s.split("\\{")[1].split("}")[0];
                            final CustomKit k = getKit(t);
                            if(k != null) {
                                s = s.replace("{" + k.getIdentifier() + "}", k.getFallenHeroName());
                            }
                        }
                        lore.add(s);
                    }
                }
                itemMeta.setLore(lore); lore.clear();
                item.setItemMeta(itemMeta);
            }
        }
        player.updateInventory();
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void inventoryClickEvent(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory top = player.getOpenInventory().getTopInventory();
        if(top.getHolder() == player) {
            final String t = event.getView().getTitle();
            if(t.equals(gui.getTitle())) {
                event.setCancelled(true);
                player.updateInventory();
                final int r = event.getRawSlot();
                final String cl = event.getClick().name();
                final CustomKit k = valueOfCustomKit(r, CustomKitMastery.class);
                final CustomKitMastery m = k != null ? (CustomKitMastery) k : null;
                if(r < 0 || r >= top.getSize() || !cl.contains("LEFT") && !cl.contains("RIGHT") || event.getCurrentItem() == null || m == null) return;
                final RPPlayer pdata = RPPlayer.get(player.getUniqueId());
                if(cl.contains("RIGHT")) {
                    preview(player, k, k.getMaxLevel());
                } else {
                    if(pdata.getKitLevels().containsKey(m)) {
                    } else {
                        sendStringListMessage(player, config.getStringList("mkits.messages.not unlocked"), null);
                    }
                }
            } else if(previewing.contains(player)) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    @EventHandler
    private void playerInteractEvent(PlayerInteractEvent event) {
        final ItemStack is = event.getItem();
        if(is != null) {
            final Player player = event.getPlayer();
            final CustomKitMastery mkit = valueOfCustomKitRedeem(is);
            if(mkit != null) {
                event.setCancelled(true);
                player.updateInventory();

                final RPPlayer pdata = RPPlayer.get(player.getUniqueId());
                final HashMap<CustomKit, Integer> required = mkit.getRequiredKits();
                final List<CustomKitGlobal> gkits = new ArrayList<>();
                final List<CustomKitEvolution> vkits = new ArrayList<>();
                for(CustomKit o : required.keySet()) {
                    if(o instanceof CustomKitGlobal) {
                        gkits.add((CustomKitGlobal) o);
                    } else if(o instanceof CustomKitEvolution) {
                        vkits.add((CustomKitEvolution) o);
                    }
                }
                CustomKitGlobal missingG = null;
                CustomKitEvolution missingV = null;
                final HashMap<CustomKit, Integer> l = pdata.getKitLevels();
                final HashMap<CustomKit, Long> cooldowns = pdata.getKitCooldowns();
                if(!gkits.isEmpty()) {
                    for(CustomKitGlobal g : gkits) {
                        if(missingG == null && (!l.containsKey(g) || l.get(g) < required.get(g))) {
                            missingG = g;
                        }
                    }
                }
                if(!vkits.isEmpty()) {
                    for(CustomKitEvolution v : vkits) {
                        if(missingV == null && (!l.containsKey(v) || l.get(v) < required.get(v))) {
                            missingV = v;
                        }
                    }
                }
                final HashMap<String, String> replacements = new HashMap<>();
                if(missingG != null) {
                    replacements.put("{KIT}", missingG.getItem().getItemMeta().getDisplayName());
                    replacements.put("{TIER}", toRoman(required.get(missingG)));
                    sendStringListMessage(player, config.getStringList("mkits.messages.unlock missing required gkit"), replacements);
                } else if(missingV != null) {
                    replacements.put("{KIT}", missingV.getItem().getItemMeta().getDisplayName());
                    replacements.put("{TIER}", toRoman(required.get(missingV)));
                    sendStringListMessage(player, config.getStringList("mkits.messages.unlock missing required vkit"), replacements);
                } else {
                    if(!gkits.isEmpty()) {
                        for(String s : colorizeListString(config.getStringList("mkits.messages.unlocked lost gkits"))) {
                            if(s.contains("{KIT}")) {
                                for(CustomKitGlobal k : gkits) {
                                    player.sendMessage(s.replace("{KIT}", k.getItem().getItemMeta().getDisplayName()));
                                }
                            } else {
                                player.sendMessage(s);
                            }
                        }
                        for(CustomKitGlobal g : gkits) {
                            l.remove(g);
                            cooldowns.remove(g);
                        }
                    }
                    if(!vkits.isEmpty()) {
                        for(String s : colorizeListString(config.getStringList("mkits.messages.unlocked lost vkits"))) {
                            if(s.contains("{KIT}")) {
                                for(CustomKitEvolution k : vkits) {
                                    player.sendMessage(s.replace("{KIT}", k.getItem().getItemMeta().getDisplayName()));
                                }
                            } else {
                                player.sendMessage(s);
                            }
                        }
                        for(CustomKitEvolution v : vkits) {
                            l.remove(v);
                            cooldowns.remove(v);
                        }
                    }
                    removeItem(player, is, 1);
                    l.put(mkit, 1);
                    replacements.put("{KIT}", mkit.getName());
                    sendStringListMessage(player, config.getStringList("mkits.messages.unlocked"), replacements);
                    player.updateInventory();
                }
            }
        }
    }
}
