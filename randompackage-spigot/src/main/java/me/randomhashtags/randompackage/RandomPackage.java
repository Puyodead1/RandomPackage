package me.randomhashtags.randompackage;

import me.randomhashtags.randompackage.api.*;
import me.randomhashtags.randompackage.api.addons.*;
import me.randomhashtags.randompackage.api.nearFinished.FactionUpgrades;
import me.randomhashtags.randompackage.api.nearFinished.Outposts;
import me.randomhashtags.randompackage.api.Wild;
import me.randomhashtags.randompackage.api.unfinished.*;
import me.randomhashtags.randompackage.events.PlayerArmorEvent;
import me.randomhashtags.randompackage.utils.CommandManager;
import me.randomhashtags.randompackage.utils.RPFeature;
import me.randomhashtags.randompackage.utils.listeners.RPEvents;
import me.randomhashtags.randompackage.utils.objects.Backup;
import me.randomhashtags.randompackage.utils.supported.RegionalAPI;
import me.randomhashtags.randompackage.utils.supported.standalone.PAPI;
import me.randomhashtags.randompackage.utils.supported.economy.VaultAPI;
import me.randomhashtags.randompackage.utils.universal.UVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;

import static me.randomhashtags.randompackage.RandomPackageAPI.spawnerchance;

public final class RandomPackage extends JavaPlugin implements Listener {
    public static RandomPackage getPlugin;

    public FileConfiguration config;

    private RandomPackageAPI api;
    private RPEvents rpevents;

    public static String spawner;
    public static Plugin spawnerPlugin, mcmmo;
    public boolean placeholderapi = false;

    private PluginManager pm;

    public void onEnable() {
        getPlugin = this;
        enable();
    }
    public void onDisable() {
        disable();
    }

    private void enable() {
        pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        checkForUpdate();
        checkFiles();
        loadSoftDepends();

        api = RandomPackageAPI.api;
        rpevents = RPEvents.getRPEvents();

        VaultAPI.getVaultAPI().setupEconomy();

        api.enable();
        getCommand("randompackage").setExecutor(api);
        rpevents.enable();
        RegionalAPI.getRegionalAPI().setup(this);

        final CommandManager cmd = CommandManager.getCommandManager(this);

        cmd.tryLoadingg(SecondaryEvents.getSecondaryEvents(), Arrays.asList("balance", "bless", "combine", "confirm", "roll", "withdraw", "xpbottle"), isTrue("balance", "bless", "combine", "roll", "withdraw", "xpbottle"));
        cmd.tryLoading(AuctionHouse.getAuctionHouse(), getHash("auctionhouse", "auction house"), isTrue("auction house"));
        cmd.tryLoading(Boosters.getBoosters(), null, isTrue("boosters"));
        cmd.tryLoading(ChatEvents.getChatEvents(), getHash("brag", "chat cmds.brag"), isTrue("chat cmds.brag", "chat cmds.item"));
        cmd.tryLoadingg(CoinFlip.getCoinFlip(), Arrays.asList("coinflip"), isTrue("coinflip"));
        cmd.tryLoading(CollectionFilter.getCollectionFilter(), getHash("collectionfilter", "collection filter"), isTrue("collection filter.enabled"));
        cmd.tryLoadingg(Conquest.getConquest(), Arrays.asList("conquest"), isTrue("conquest"));
        cmd.tryLoadingg(CustomArmor.getCustomArmor(), null, isTrue("custom armor"));
        cmd.tryLoading(CustomBosses.getCustomBosses(), null, isTrue("custom bosses"));

        cmd.tryLoading(CustomEnchants.getCustomEnchants(), getHash("alchemist", "alchemist", "disabledenchants", "disabled enchants", "enchanter", "enchanter", "enchants", "enchants", "tinkerer", "tinkerer"), isTrue("alchemist", "disabled enchants", "enchanter", "enchants", "tinkerer"));
        cmd.tryLoadingg(BlackScrolls.getBlackScrolls(), null, isTrue("custom enchants.blacks scroll", true));
        cmd.tryLoadingg(EnchantmentOrbs.getEnchantmentOrbs(), null, isTrue("custom enchants.enchantment orbs", true));
        cmd.tryLoadingg(Fireballs.getFireballs(), null, isTrue("custom enchants.fireballs", true));
        cmd.tryLoadingg(RandomizationScrolls.getRandomizationScrolls(), null, isTrue("custom enchants.randomization scrolls", true));
        cmd.tryLoadingg(RarityGems.getRarityGems(), null, isTrue("custom enchants.rarity gems", true));
        cmd.tryLoadingg(SoulTrackers.getSoulTrackers(), Arrays.asList("splitsouls"), isTrue("custom enchants.soul trackers", true) || isTrue("splitsouls"));
        cmd.tryLoadingg(TransmogScrolls.getTransmogScrolls(), null, isTrue("custom enchants.transmog scrolls", true));
        cmd.tryLoadingg(WhiteScrolls.getWhiteScrolls(), null, isTrue("custom enchants.white scrolls", true));

        cmd.tryLoading(CustomExplosions.getCustomExplosions(), null, isTrue("custom creepers", "custom tnt"));
        cmd.tryLoading(Duels.getDuels(), getHash("duel", "duels"), isTrue("duels"));
        cmd.tryLoading(Dungeons.getDungeons(), getHash("dungeon", "dungeons"), isTrue("dungeons"));
        cmd.tryLoadingg(Envoy.getEnvoy(), Arrays.asList("envoy"), isTrue("envoy"));
        cmd.tryLoading(FactionUpgrades.getFactionUpgrades(), null, isTrue("faction upgrades"));
        cmd.tryLoadingg(Fund.getFund(), Arrays.asList("fund"), isTrue("fund"));
        cmd.tryLoading(GlobalChallenges.getChallenges(), getHash("challenge", "global challenges"), isTrue("global challenges"));
        cmd.tryLoadingg(Homes.getHomes(), Arrays.asList("home"), isTrue("home"));
        cmd.tryLoadingg(ItemFilter.getItemFilter(), Arrays.asList("filter"), isTrue("filter"));
        cmd.tryLoadingg(Jackpot.getJackpot(), Arrays.asList("jackpot"), isTrue("jackpot"));

        cmd.tryLoading(KitsEvolution.getKitsEvolution(), getHash("vkit", "vkits"), isTrue("vkits"));
        cmd.tryLoading(KitsGlobal.getKitsGlobal(), getHash("gkit", "gkits"), isTrue("gkits"));
        cmd.tryLoading(KitsMastery.getKitsMastery(), getHash("mkit", "mkits"), isTrue("mkits"));

        cmd.tryLoadingg(KOTH.getKOTH(), Arrays.asList("kingofthehill"), isTrue("kingofthehill"));
        cmd.tryLoading(LastManStanding.getLastManStanding(), getHash("lastmanstanding", "last man standing"), isTrue("last man standing"));
        cmd.tryLoadingg(Masks.getMasks(), null, isTrue("masks"));
        cmd.tryLoadingg(MobStacker.getMobStacker(), null, isTrue("mob stacker"));
        cmd.tryLoading(Outposts.getOutposts(), getHash("outpost", "outposts"), isTrue("outposts"));
        cmd.tryLoadingg(InventoryPets.getInventoryPets(), null, isTrue("pets"));
        cmd.tryLoadingg(Trinkets.getTrinkets(), null, isTrue("trinkets"));
        cmd.tryLoading(MonthlyCrates.getMonthlyCrates(), getHash("monthlycrate", "monthly crates"), isTrue("monthly crates"));
        cmd.tryLoadingg(ServerCrates.getServerCrates(), null, isTrue("server crates"));
        cmd.tryLoadingg(Titles.getTitles(), Arrays.asList("title"), isTrue("title"));
        cmd.tryLoading(Lootboxes.getLootboxes(), getHash("lootbox", "lootboxes"), isTrue("lootboxes"));
        cmd.tryLoadingg(Shop.getShop(), Arrays.asList("shop"), isTrue("shop"));
        cmd.tryLoadingg(Showcase.getShowcase(), Arrays.asList("showcase"), isTrue("showcase"));
        cmd.tryLoading(PlayerQuests.getPlayerQuests(), getHash("quest", "player quests"), isTrue("player quests"));
        cmd.tryLoadingg(Trade.getTrade(), Arrays.asList("trade"), isTrue("trade"));
        cmd.tryLoadingg(Wild.getWild(), Arrays.asList("wild"), isTrue("wild"));
        cmd.tryLoading(WildPvP.getWildPvP(), getHash("wildpvp", "wild pvp"), isTrue("wild pvp"));

        final int interval = config.getInt("backup interval")*20*60;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ()-> new Backup(), interval, interval);
    }
    private boolean isTrue(String path, boolean exact) {
        return config.getBoolean(path + (exact ? "" : ".enabled"));
    }
    private boolean isTrue(String...paths) {
        boolean enabled = false;
        for(String s : paths) {
            if(config.getBoolean(s + ".enabled")) {
                enabled = true;
                break;
            }
        }
        return enabled;
    }
    private HashMap<String, String> getHash(String...values) {
        final HashMap<String, String> a = new HashMap<>();
        for(int i = 0; i < values.length; i++) {
            if(i%2 == 1) {
                a.put(values[i-1], values[i]);
            }
        }
        return a;
    }

    private void checkFiles() {
        UVersion.getUVersion().save(null, "config.yml");
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
    }
    private void loadSoftDepends() {
        tryLoadingMCMMO();
        tryLoadingSpawner();
        if(isTrue("supported plugins.standalone.PlaceholderAPI", true) && pm.isPluginEnabled("PlaceholderAPI")) {
            placeholderapi = true;
            PAPI.getPAPI();
        }
    }
    public void tryLoadingMCMMO() {
        if(isTrue("supported plugins.mechanics.MCMMO", true) && pm.isPluginEnabled("mcMMO")) {
            mcmmo = pm.getPlugin("mcMMO");
        }
    }
    public void tryLoadingSpawner() {
        final String ss = isTrue("supported plugins.mechanics.SilkSpawners", true) && pm.isPluginEnabled("SilkSpawners") ? "SilkSpawners" : null;
        final String es = isTrue("supported plugins.mechanics.EpicSpawners", true) && pm.isPluginEnabled("EpicSpawners") ? "EpicSpawners" + (pm.getPlugin("EpicSpawners").getDescription().getVersion().startsWith("5") ? "5" : "6") : null;
        final boolean epic = es != null;
        if(epic || ss != null) {
            spawnerPlugin = pm.getPlugin(epic ? "EpicSpawners" : "SilkSpawners");
            spawner = epic ? es : ss;
            final FileConfiguration c = spawnerPlugin.getConfig();
            spawnerchance = epic ? Integer.parseInt(c.getString("Spawner Drops.Chance On TNT Explosion").replace("%", "")): c.getInt("explosionDropChance");
        }
    }
    private void disable() {
        rpevents.disable();
        api.disable();

        CommandManager.getCommandManager(null).disable();
        RPFeature.d();
        HandlerList.unregisterAll((Listener) this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    public void checkForUpdate() {
        final BukkitScheduler s = Bukkit.getScheduler();
        final int l = 20*60*15;
        s.scheduleSyncRepeatingTask(this, () -> {
            s.runTaskAsynchronously(this, () -> {
                String msg = null;
                try {
                    final URL checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=38501");
                    final URLConnection con = checkURL.openConnection();
                    final String v = RandomPackage.getPlugin.getDescription().getVersion(), newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
                    final boolean canUpdate = !v.equals(newVersion);
                    if(canUpdate) {
                        msg = ChatColor.translateAlternateColorCodes('&', "&6[RandomPackage] &eUpdate available! &aYour version: &f" + v + "&a. Latest version: &f" + newVersion);
                    }
                } catch(Exception e) {
                    msg = ChatColor.translateAlternateColorCodes('&', "&6[RandomPackage] &cCould not check for updates due to being unable to connect to SpigotMC!");
                }
                if(msg != null) {
                    Bukkit.getConsoleSender().sendMessage(msg);
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        if(p.isOp()) p.sendMessage(msg);
                    }
                }
            });
        }, 0, l);
    }
    public void reload() {
        disable();
        enable();
    }

    /*
     *
     * PlayerArmorEvent Listener
     *
     */
    @EventHandler
    private void inventoryClickEvent(InventoryClickEvent event) {
        if(!event.isCancelled() && !event.getClick().equals(ClickType.DOUBLE_CLICK) && event.getCurrentItem() != null && event.getCursor() != null && event.getInventory().getType().equals(InventoryType.CRAFTING)) {
            final Player player = (Player) event.getWhoClicked();
            final InventoryType.SlotType st = event.getSlotType();
            final ClickType ct = event.getClick();
            final ItemStack cursoritem = event.getCursor(), currentitem = event.getCurrentItem();
            final PlayerInventory inv = player.getInventory();
            final String cursor = cursoritem.getType().name(), current = currentitem.getType().name();
            if((st.equals(InventoryType.SlotType.QUICKBAR) || st.equals(InventoryType.SlotType.CONTAINER)) && ct.equals(ClickType.CONTROL_DROP)) return;
            PlayerArmorEvent a = null, b = null;
            if(st.equals(InventoryType.SlotType.ARMOR) && ct.equals(ClickType.NUMBER_KEY)) {
                final int rawslot = event.getRawSlot();
                final ItemStack prev = inv.getItem(event.getSlot()), hb = inv.getItem(event.getHotbarButton());
                final String t = hb != null ? hb.getType().name() : "AIR";
                if(prev != null && !prev.getType().name().equals("AIR"))
                    a = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.NUMBER_KEY_UNEQUIP, prev);
                if(canBeUsed(rawslot, t))
                    b = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.NUMBER_KEY_EQUIP, hb);
            } else if(event.isShiftClick()) {
                if(st.equals(InventoryType.SlotType.ARMOR))
                    a = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.SHIFT_UNEQUIP, currentitem);
                else {
                    final int t = getTargetSlot(current);
                    if(t == -1) return;
                    final ItemStack prevArmor = inv.getArmorContents()[t == 5 ? 3 : t == 6 ? 2 : t == 7 ? 1 : 0];
                    if((prevArmor == null || prevArmor.getType().equals(Material.AIR)) && canBeUsed(t, current)) a = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.SHIFT_EQUIP, currentitem);
                }
            } else if(st.equals(InventoryType.SlotType.ARMOR)) {
                if(ct.name().contains("DROP") && !current.equals("AIR")) {
                    a = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.DROP, currentitem);
                } else if(ct.equals(ClickType.LEFT) || ct.equals(ClickType.RIGHT)) {
                    final int rawslot = event.getRawSlot();
                    if(!current.equals("AIR")) {
                        final int c1 = getTargetSlot(current), c2 = getTargetSlot(cursor);
                        if(c1 == c2 || rawslot == c1)
                            a = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.INVENTORY_UNEQUIP, currentitem);
                    }
                    if(!cursor.equals("AIR")) {
                        final int c1 = getTargetSlot(current), c2 = getTargetSlot(cursor);
                        if(c1 == c2 || rawslot == c2)
                            b = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.INVENTORY_EQUIP, cursoritem);
                    }
                }
            }
            if(a != null) {
                pm.callEvent(a);
                final ItemStack y = a.getCurrentItem(), z = a.getCursor();
                if(y != null) event.setCurrentItem(y);
                if(z != null) event.setCursor(z);
            }
            if(b != null) {
                pm.callEvent(b);
                final ItemStack y = b.getCurrentItem(), z = b.getCursor();
                if(y != null) event.setCurrentItem(y);
                if(z != null) event.setCursor(z);
            }
        }
    }
    private int getTargetSlot(String target) {
        return target.contains("HELMET") || target.contains("SKULL") || target.contains("HEAD") ? 5
                : target.contains("CHESTPLATE") || target.contains("ELYTRA") ? 6
                : target.contains("LEGGINGS") ? 7
                : target.contains("BOOTS") ? 8
                : -1;
    }
    private boolean canBeUsed(int rawslot, String target) {
        return rawslot == 5 && (target.contains("HELMET") || target.contains("SKULL") || target.contains("HEAD"))
                || rawslot == 6 && (target.contains("CHESTPLATE") || target.contains("ELYTRA"))
                || rawslot == 7 && target.contains("LEGGINGS")
                || rawslot == 8 && target.contains("BOOTS");
    }
    @EventHandler
    private void playerInteractEvent(PlayerInteractEvent event) {
        if(event.getItem() != null && event.getAction().name().contains("RIGHT")) {
            final ItemStack i = event.getItem().clone();
            final String item = i.getType().name();
            final Player player = event.getPlayer();
            final PlayerInventory PI = player.getInventory();
            if(item.endsWith("HELMET") && PI.getHelmet() == null || item.endsWith("CHESTPLATE") && PI.getChestplate() == null || item.endsWith("LEGGINGS") && PI.getLeggings() == null || item.endsWith("BOOTS") && PI.getBoots() == null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                    if(!player.getGameMode().equals(GameMode.CREATIVE) && player.getItemInHand().equals(i)) return;
                    PlayerArmorEvent armorevent = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.HOTBAR_EQUIP, i);
                    pm.callEvent(armorevent);
                }, 0);
            } else if(item.endsWith("HELMET") && PI.getHelmet() != null || item.endsWith("CHESTPLATE") && PI.getChestplate() != null || item.endsWith("LEGGINGS") && PI.getLeggings() != null || item.endsWith("BOOTS") && PI.getBoots() != null) {
                PlayerArmorEvent armorevent = new PlayerArmorEvent(player, PlayerArmorEvent.ArmorEventReason.HOTBAR_SWAP, i);
                pm.callEvent(armorevent);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void playerItemBreakEvent(PlayerItemBreakEvent event) {
        final ItemStack i = event.getBrokenItem();
        if(i.getType().name().endsWith("HELMET") || i.getType().name().endsWith("CHESTPLATE") || i.getType().name().endsWith("LEGGINGS") || i.getType().name().endsWith("BOOTS")) {
            PlayerArmorEvent armorevent = new PlayerArmorEvent(event.getPlayer(), PlayerArmorEvent.ArmorEventReason.BREAK, i);
            pm.callEvent(armorevent);
        }
    }
}
