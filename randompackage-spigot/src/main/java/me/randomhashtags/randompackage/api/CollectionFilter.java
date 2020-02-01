package me.randomhashtags.randompackage.api;

import com.sun.istack.internal.NotNull;
import me.randomhashtags.randompackage.addon.obj.CollectionChest;
import me.randomhashtags.randompackage.universal.UInventory;
import me.randomhashtags.randompackage.universal.UMaterial;
import me.randomhashtags.randompackage.util.RPFeature;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CollectionFilter extends RPFeature implements CommandExecutor {
    private static CollectionFilter instance;
    public static CollectionFilter getCollectionFilter() {
        if(instance == null) instance = new CollectionFilter();
        return instance;
    }

    public YamlConfiguration config;
    private ItemStack collectionchest;
    public String defaultType, allType, itemType, filtertypeString;
    private String selectedPrefix, notSelectedPrefix;
    public int filtertypeSlot = -1;

    private UInventory collectionchestgui;
    private Material allMaterial;
    private HashMap<Integer, UMaterial> picksup;
    private HashMap<UUID, Location> editingfilter;

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final Player player = sender instanceof Player ? (Player) sender : null;
        if(player != null && hasPermission(sender, "RandomPackage.collectionfilter", true)) {
            final int length = args.length;
            if(length == 0 || length == 1 || length == 2 && !args[1].equals("all")) {
                final String arg = length >= 1 ? length == 1 ? args[0] : args[0] + "_" + args[1] : "";
                final ItemStack q = getItemInHand(player);
                if(q.getType().equals(collectionchest.getType()) && q.hasItemMeta() && q.getItemMeta().getDisplayName() != null && q.getItemMeta().getDisplayName().equals(collectionchest.getItemMeta().getDisplayName())) {
                    switch (arg) {
                        case "default":
                            setFilter(player, q, defaultType);
                            break;
                        case "all":
                            setFilter(player, q, colorize(config.getString("collection chests.chest.filter types.all")));
                            break;
                        default:
                            if(length == 0) {
                                editFilter(player, null);
                            } else {
                                Material f = Material.getMaterial(arg.toUpperCase());
                                if(f != null) {
                                    setFilter(player, q, itemType.replace("{ITEM}", toMaterial(arg, false)));
                                } else {
                                    sendStringListMessage(player, getStringList(config, "messages.invalid filter type"), null);
                                    editFilter(player, null);
                                }
                            }
                            break;
                    }
                } else {
                    if(length == 1) {
                        sendStringListMessage(player, getStringList(config, "messages.invalid filter type"), null);
                    }
                    sendStringListMessage(sender, getStringList(config, "messages.need to be holding cc"), null);
                }
            } else if(length == 2 && args[1].equals("all") || length == 3 && args[2].equals("all")) {

            }
        }
        return true;
    }

    public String getIdentifier() {
        return "COLLECTION_FILTER";
    }
    public void load() {
        final long started = System.currentTimeMillis();
        save(null, "collection filter.yml");
        config = YamlConfiguration.loadConfiguration(new File(DATA_FOLDER, "collection filter.yml"));

        collectionchest = d(config, "collection chests.chest");
        allType = colorize(config.getString("collection chests.chest.filter types.all"));
        defaultType = colorize(config.getString("collection chests.chest.filter types.default"));
        itemType = colorize(config.getString("collection chests.chest.filter types.item"));
        selectedPrefix = colorize(config.getString("gui.selected.prefix"));
        notSelectedPrefix = colorize(config.getString("gui.not selected.prefix"));

        picksup = new HashMap<>();
        editingfilter = new HashMap<>();

        collectionchestgui = new UInventory(null, config.getInt("gui.size"), colorize(config.getString("gui.title")));
        final Inventory inv = collectionchestgui.getInventory();
        final ItemStack background = d(config, "gui.background");
        for(int i = 0; i < collectionchestgui.getSize(); i++) {
            if(config.get("gui." + i) != null) {
                final ItemStack itemstack = d(config, "gui." + i);
                inv.setItem(i, itemstack);
                final String[] j = config.getString("gui." + i + ".picks up").toUpperCase().split(":");
                final UMaterial um = UMaterial.match(j[0], j.length > 1 ? Byte.parseByte(j[1]) : 0);
                if(um == null && j[0].equalsIgnoreCase("all")) {
                    allMaterial = itemstack.getType();
                }
                picksup.put(i, um);
            } else {
                inv.setItem(i, background);
            }
        }
        final List<String> lore = collectionchest.getItemMeta().getLore();
        for(int i = 0; i < lore.size(); i++) {
            if(lore.get(i).contains("{FILTER_TYPE}")) {
                filtertypeSlot = i;
                filtertypeString = lore.get(i);
            }
        }
        sendConsoleDidLoadFeature("Collection Filter", started);

        SCHEDULER.runTaskAsynchronously(RANDOM_PACKAGE, () -> {
            for(String s : getConfigurationSectionKeys(otherdata, "collection chests", false)) {
                final String[] info = otherdata.getString("collection chests." + s + ".info").split(":");
                new CollectionChest(UUID.fromString(s), info[0], toLocation(info[1]), !info[2].equals("null") ? UMaterial.match(info[2]) : null);
            }
            final HashMap<UUID, CollectionChest> chests = CollectionChest.chests;
            sendConsoleDidLoadAsyncFeature((chests != null ? chests.size() : 0) + " collection chests");
        });
    }
    public void unload() {
        for(UUID u : new ArrayList<>(editingfilter.keySet())) {
            final OfflinePlayer o = Bukkit.getOfflinePlayer(u);
            if(o.isOnline()) {
                o.getPlayer().closeInventory();
            }
        }
        otherdata.set("collection chests", null);
        final HashMap<UUID, CollectionChest> chests = CollectionChest.chests;
        if(chests != null) {
            for(CollectionChest c : chests.values()) {
                c.backup();
            }
        }
        CollectionChest.deleteAll();
    }

    private void viewFilter(Player player, CollectionChest chest) {
        final HashMap<String, String> replacements = new HashMap<>();
        final UMaterial f = chest.getFilter();
        replacements.put("{ITEM}", f != null ? f.name() : "All");
        sendStringListMessage(player, getStringList(config, "messages.view filter"), replacements);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void entityDeathEvent(EntityDeathEvent event) {
        final Entity e = event.getEntity();
        final HashMap<UUID, CollectionChest> chests = CollectionChest.chests;
        if(chests != null && getStringList(config, "enabled worlds").contains(e.getWorld().getName())) {
            final Chunk chunk = e.getLocation().getChunk();
            final List<ItemStack> drops = event.getDrops();
            for(CollectionChest chest : chests.values()) {
                if(chest.getLocation().getChunk().equals(chunk)) {
                    final UMaterial filter = chest.getFilter();
                    final Inventory inv = chest.getInventory();
                    final List<ItemStack> added = new ArrayList<>();
                    for(ItemStack is : drops) {
                        if(filter == null || filter.equals(UMaterial.match(is))) {
                            inv.addItem(is);
                            added.add(is);
                        }
                    }
                    drops.removeAll(added);
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void playerInteractEvent(PlayerInteractEvent event) {
        final Block b = event.getClickedBlock();
        if(b != null && !b.getType().equals(Material.AIR) && b.getType().name().contains("CHEST")) {
            final Player player = event.getPlayer();
            final CollectionChest chest = CollectionChest.valueOf(b);
            if(chest != null) {
                switch (event.getAction()) {
                    case RIGHT_CLICK_BLOCK:
                        if(player.isSneaking()) {
                            event.setCancelled(true);
                            editFilter(player, b);
                        } else {
                            chest.getInventory();
                        }
                        break;
                    case LEFT_CLICK_BLOCK:
                        viewFilter(player, chest);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void blockPlaceEvent(BlockPlaceEvent event) {
        final ItemStack is = event.getItemInHand();
        if(UMaterial.match(is).equals(UMaterial.match(collectionchest)) && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equals(collectionchest.getItemMeta().getDisplayName())) {
            final Player player = event.getPlayer();
            item = is.clone(); item.setAmount(1);
            final UMaterial material = getFiltered(item);
            new CollectionChest(player.getUniqueId().toString(), event.getBlockPlaced().getLocation(), material);
            sendStringListMessage(player, getStringList(config, "messages.placed"), null);
            final String f = material == null ? defaultType : toMaterial(material.getMaterial().name(), false);
            for(String s : getStringList(config, "messages.set")) {
                if(s.contains("{ITEM}")) s = s.replace("{ITEM}", f);
                player.sendMessage(colorize(s));
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void blockBreakEvent(BlockBreakEvent event) {
        final Block block = event.getBlock();
        if(block.getType().equals(collectionchest.getType())) {
            final CollectionChest chest = CollectionChest.valueOf(block);
            if(chest != null) {
                event.setCancelled(true);
                block.setType(Material.AIR);
                if(config.getBoolean("collection chests.chest.keeps meta")) {
                    final UMaterial u = chest.getFilter();
                    item = collectionchest.clone();
                    itemMeta = item.getItemMeta();
                    lore = itemMeta.getLore();
                    lore.set(filtertypeSlot, filtertypeString.replace("{FILTER_TYPE}", u == null ? allType : itemType.replace("{ITEM}", toMaterial(u.name(), false))));
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
                chest.destroy();
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void inventoryClickEvent(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory top = player.getOpenInventory().getTopInventory();
        if(top.getHolder() != null && top.getHolder().equals(player) && event.getView().getTitle().equals(collectionchestgui.getTitle())) {
            event.setCancelled(true);
            player.updateInventory();

            final int slot = event.getRawSlot();
            final ItemStack current = event.getCurrentItem();
            if(current == null || current.getType().equals(Material.AIR) || slot >= top.getSize()) return;
            final UUID uuid = player.getUniqueId();
            final CollectionChest chest = editingfilter.containsKey(uuid) ? CollectionChest.valueOf(player.getWorld().getBlockAt(editingfilter.get(uuid))) : null;
            final UMaterial filter = chest != null ? chest.getFilter() : null;
            if(filter != null && filter.equals(UMaterial.match(current))) {
                sendStringListMessage(player, getStringList(config, "messages.item already being filtered"), null);
            } else {
                if(chest != null && editingfilter.containsKey(uuid)) {
                    chest.setFilter(picksup.get(slot));
                    editingfilter.remove(uuid);
                    viewFilter(player, chest);
                } else {
                    setFilter(player, getItemInHand(player), slot);
                }
                player.closeInventory();;
            }
        }
    }
    public void editFilter(@NotNull Player player, Block clickedblock) {
        if(clickedblock != null) {
            editingfilter.put(player.getUniqueId(), clickedblock.getLocation());
        }
        player.openInventory(Bukkit.createInventory(player, collectionchestgui.getSize(), collectionchestgui.getTitle()));
        final Inventory top = player.getOpenInventory().getTopInventory();
        top.setContents(collectionchestgui.getInventory().getContents());
        player.updateInventory();
        final boolean selectedEnchanted = config.getBoolean("gui.selected.enchanted"), notselectedEnchanted = config.getBoolean("gui.not selected.enchanted");
        final CollectionChest chest = CollectionChest.valueOf(clickedblock);
        final UMaterial filter = chest != null ? chest.getFilter() : getFiltered(player.getItemInHand());
        final Material filterMaterial = filter != null ? filter.getMaterial() : null;
        final List<String> selectedLore = getStringList(config, "gui.selected.added lore"), notSelectedLore = getStringList(config, "gui.not selected.added lore");
        for(int i = 0; i < top.getSize(); i++) {
            item = top.getItem(i);
            if(item != null && !item.getType().equals(Material.AIR)) {
                final UMaterial target = picksup.get(i);
                final String umaterial = toMaterial(target != null ? target.name() : allMaterial.name(), false);
                itemMeta = item.getItemMeta(); lore.clear();
                final boolean isSelected = filter == null && item.getType().equals(allMaterial) || filter != null && target.equals(filterMaterial), isEnchanted = isSelected && selectedEnchanted || !isSelected && notselectedEnchanted;
                final String name = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : itemMeta.hasEnchants() ? ChatColor.AQUA + umaterial : umaterial;
                itemMeta.setDisplayName((isSelected ? selectedPrefix : notSelectedPrefix) + name);
                if(itemMeta.hasLore()) {
                    lore.addAll(itemMeta.getLore());
                }
                lore.addAll(isSelected ? selectedLore : notSelectedLore);
                if(isEnchanted) {
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                itemMeta.setLore(lore); lore.clear();
                item.setItemMeta(itemMeta);
                if(isEnchanted) {
                    item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
                }
            }
        }
        player.updateInventory();
    }
    public ItemStack getCollectionChest(@NotNull String filter) {
        filter = filter.toLowerCase();
        filter = filter.equals("all") ? allType : filter.equals("default") ? defaultType : toMaterial(filter, false);
        item = collectionchest.clone(); itemMeta = item.getItemMeta(); lore.clear();
        if(itemMeta.hasLore()) {
            for(String s : itemMeta.getLore()) {
                if(s.contains("{FILTER_TYPE}")) s = s.replace("{FILTER_TYPE}", filter);
                lore.add(s);
            }
        }
        itemMeta.setLore(lore); lore.clear();
        item.setItemMeta(itemMeta);
        return item;
    }
    public void setFilter(@NotNull Player player, @NotNull ItemStack is, int slot) {
        itemMeta = is.getItemMeta(); lore.clear();
        final List<String> l = collectionchest.getItemMeta().getLore();
        final String m = toMaterial(picksup.get(slot).name(), false);
        for(int i = 0; i < l.size(); i++) {
            final String target = l.get(i);
            if(i == filtertypeSlot) {
                lore.add(colorize(target.replace("{FILTER_TYPE}", m)));
            } else {
                lore.add(target);
            }
        }
        itemMeta.setLore(lore);
        is.setItemMeta(itemMeta);
        lore.clear();
        for(String string : getStringList(config, "messages.updated cc")) {
            if(string.contains("{AMOUNT}")) string = string.replace("{AMOUNT}", Integer.toString(is.getAmount()));
            if(string.contains("{ITEM}")) string = string.replace("{ITEM}", m);
            player.sendMessage(colorize(string));
        }
        player.updateInventory();
    }
    public void setFilter(@NotNull Player player, @NotNull ItemStack is, @NotNull String filter) {
        itemMeta = is.getItemMeta(); lore.clear();
        if(itemMeta.hasLore()) {
            lore.addAll(itemMeta.getLore());
        }
        lore.set(filtertypeSlot, collectionchest.getItemMeta().getLore().get(filtertypeSlot).replace("{FILTER_TYPE}", filter));
        itemMeta.setLore(lore);
        is.setItemMeta(itemMeta);
        lore.clear();
        for(String string : getStringList(config, "messages.set")) {
            if(string.contains("{ITEM}")) string = string.replace("{ITEM}", filter);
            player.sendMessage(colorize(string));
        }
    }
    public UMaterial getFiltered(@NotNull ItemStack is) {
        final String filter = ChatColor.stripColor(collectionchest.clone().getItemMeta().getLore().get(filtertypeSlot).replace("{FILTER_TYPE}", itemType)), filterString = ChatColor.stripColor(is.getItemMeta().getLore().get(filtertypeSlot).toUpperCase());
        for(UMaterial s : picksup.values()) {
            if(s != null && filterString.equals(filter.replace("{ITEM}", s.name().replace("_", " ")))) {
                return s;
            }
        }
        return null;
    }
}
