package me.randomhashtags.randompackage.api;

import me.randomhashtags.randompackage.RandomPackageAPI;
import me.randomhashtags.randompackage.api.events.envoy.PlayerClaimEnvoyCrateEvent;
import me.randomhashtags.randompackage.utils.classes.envoy.EnvoyCrate;
import me.randomhashtags.randompackage.utils.classes.envoy.LivingEnvoyCrate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.*;

import static me.randomhashtags.randompackage.utils.classes.envoy.EnvoyCrate.getRandomCrate;

public class Envoy extends RandomPackageAPI {

	private static Envoy instance;
	public static Envoy getEnvoy() {
		if(instance == null) instance = new Envoy();
		return instance;
	}
	
	public YamlConfiguration config;
	public boolean isEnabled = false;

	public ItemStack envoySummon, presetLocationPlacer;
	private int spawnTask, task, totalEnvoys = 0;
	private String type;
	public List<Location> preset;
	private List<Player> settingPreset;

	public HashMap<Integer, HashMap<Location, EnvoyCrate>> active;

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		final Player player = sender instanceof Player ? (Player) sender : null;
		final int l = args.length;
		if(l == 0) {
			
		} else {
		    final String a = args[0];
			if(a.equals("help")) viewHelp(sender);
			else if(a.equals("spawn") || a.equals("summon") || a.equals("begin") || a.equals("start")) {
				if(hasPermission(sender, "RandomPackage.envoy.start", true))
					spawnEnvoy(translateColorCodes(config.getString("messages.default summon type")), false, l == 1 ? type : args[1].toUpperCase());
			} else if(a.equals("stop") || a.equals("end")) {
				if(hasPermission(sender, "RandomPackage.envoy.stop", true)) stopAllEnvoys();
            } else if(player != null && a.equals("preset")) {
			    enterEditMode(player);
            }
		}
		return true;
	}

	public void enable() {
		final long started = System.currentTimeMillis();
		if(isEnabled) return;
		save(null, "envoy.yml");
		eventmanager.registerListeners(randompackage, this);
		isEnabled = true;

		preset = new ArrayList<>();
		settingPreset = new ArrayList<>();
		active = new HashMap<>();

		final YamlConfiguration a = otherdata;
		final List<String> c = a.getStringList("envoy.preset");
		if(c != null && !c.isEmpty())
			for(String s : c)
				preset.add(toLocation(s));
		config = YamlConfiguration.loadConfiguration(new File(rpd, "envoy.yml"));
		EnvoyCrate.defaultTier = config.getString("settings.default tier");
		type = config.getString("settings.type");
		envoySummon = d(config, "items.envoy summon");

		givedpitem.items.put("envoysummon", envoySummon);

		presetLocationPlacer = new ItemStack(Material.BEDROCK);
		itemMeta = presetLocationPlacer.getItemMeta();
		itemMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Preset EnvoyCrate Location");
		itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "Place me to add a preset envoy location for", ChatColor.GRAY + "a chance for an EnvoyCrate to spawn at this location."));
		presetLocationPlacer.setItemMeta(itemMeta);

		if(!a.getBoolean("saved default envoy tiers")) {
			final String[] e = new String[]{"ELITE", "LEGENDARY", "SIMPLE", "ULTIMATE", "UNIQUE"};
			for(String s : e) save("envoy tiers", s + ".yml");
			a.set("saved default envoy tiers", true);
			saveOtherData();
		}

		final List<ItemStack> tiers = new ArrayList<>();

		for(File f : new File(rpd + separator + "envoy tiers").listFiles()) {
			final EnvoyCrate e = new EnvoyCrate(f);
			tiers.add(e.getItem());
		}
		addGivedpCategory(tiers, UMaterial.ENDER_CHEST, "Envoy Tiers", "Givedp: Envoy Tiers");
		final String defaul = translateColorCodes(config.getString("messages.default summon type"));

		spawnTask = scheduler.scheduleSyncDelayedTask(randompackage, () -> spawnEnvoy(defaul, true, type), getRandomTime());
		task = scheduler.scheduleSyncRepeatingTask(randompackage, () -> {
			for(int i : active.keySet()) {
				final HashMap<Location, EnvoyCrate> w = active.get(i);
				for(Location l : w.keySet()) {
					spawnFirework(w.get(l).getFirework(), l);
				}
			}
		}, 0, 20*config.getInt("settings.firework delay"));

		final HashMap<String, EnvoyCrate> E = EnvoyCrate.crates;
		sendConsoleMessage("&6[RandomPackage] &aLoaded " + (E != null ? E.size() : 0) + " envoy tiers &e(took " + (System.currentTimeMillis()-started) + "ms)");
	}
	public void disable() {
		if(!isEnabled) return;
		final List<String> p = new ArrayList<>();
		for(Location l : preset) p.add(toString(l));
		otherdata.set("envoy.preset", p);
		saveOtherData();
		if(!settingPreset.isEmpty()) {
			for(Player player : settingPreset) player.getInventory().remove(presetLocationPlacer);
			for(Location l : preset) l.getWorld().getBlockAt(l).setType(Material.AIR);
		}
		scheduler.cancelTask(spawnTask);
		scheduler.cancelTask(task);
		stopAllEnvoys();
		config = null;
		envoySummon = null;
		givedpitem.items.remove("envoysummon");
		presetLocationPlacer = null;
		spawnTask = 0;
		task = 0;
		type = null;
		preset = null;
		settingPreset = null;
		active = null;
		isEnabled = false;
		EnvoyCrate.deleteAll();
		eventmanager.unregisterListeners(this);
	}

	public void stopAllEnvoys() {
		final HashMap<Integer, HashMap<Location, LivingEnvoyCrate>> L = LivingEnvoyCrate.living;
		if(L != null) {
			final HashMap<Integer, HashMap<Location, LivingEnvoyCrate>> l = new HashMap<>(L);
			for(int i : l.keySet()) {
				final HashMap<Location, LivingEnvoyCrate> a = new HashMap<>(L.get(i));
				for(LivingEnvoyCrate e : a.values())
					e.delete(false);
			}
		}
	}
	
	@Listener
	private void playerInteractEvent(PlayerInteractEvent event) {
		final ItemStack i = event.getItem();
		final EnvoyCrate ec = EnvoyCrate.valueOf(i);
		final Player player = event.getPlayer();
		if(ec != null) {
			event.setCancelled(true);
			player.updateInventory();
			removeItem(player, i, 1);
			final List<String> rewards = ec.getRandomRewards();
			for(String s : rewards) giveItem(player, d(null, s));
		} else if(i != null && i.hasItemMeta() && i.getItemMeta().equals(envoySummon.getItemMeta())) {
			event.setCancelled(true);
			player.updateInventory();
			removeItem(player, i, 1);
			spawnEnvoy(translateColorCodes(config.getString("messages.item summon type").replace("{PLAYER}", player.getName())), false, type);
		} else if(event.getClickedBlock() != null) {
			final Location l = event.getClickedBlock().getLocation();
			final LivingEnvoyCrate c = LivingEnvoyCrate.valueOf(l);
			if(c != null) {
				final PlayerClaimEnvoyCrateEvent e = new PlayerClaimEnvoyCrateEvent(player, l, c);
				pluginmanager.callEvent(e);
				if(!e.isCancelled()) {
					event.setCancelled(true);
					player.updateInventory();
					c.delete(true);
				}
			}
		}
	}
	@Listener(priority = EventPriority.HIGHEST)
    private void blockPlaceEvent(BlockPlaceEvent event) {
	    if(!event.isCancelled()) {
	        final Player player = event.getPlayer();
	        if(settingPreset.contains(player) && player.getItemInHand().equals(presetLocationPlacer)) {
	            preset.add(event.getBlockPlaced().getLocation());
            }
        }
    }
    @Listener
	private void blockBreakEvent(BlockBreakEvent event) {
		if(!event.isCancelled() && settingPreset.contains(event.getPlayer())) {
			preset.remove(event.getBlock().getLocation());
		}
	}

    public void enterEditMode(Player player) {
		if(hasPermission(player, "RandomPackage.envoy.preset", true)) {
			final CarriedInventory i = player.getInventory();
			if(!settingPreset.contains(player)) {
				if(settingPreset.isEmpty()) for(Location l : preset) l.getWorld().getBlockAt(l).setType(Material.BEDROCK);
				settingPreset.add(player);
				i.addItem(presetLocationPlacer);
			} else {
				settingPreset.remove(player);
				i.remove(presetLocationPlacer);
				if(settingPreset.isEmpty()) for(Location l : preset) l.getWorld().getBlockAt(l).setType(Material.AIR);
			}
		}
    }

	public void spawnEnvoy(String type, int amount) {
		type = type.toUpperCase();
		final Random random = new Random();
		final int despawn = config.getInt("settings.availability");
		if(type.equals("WARZONE")) {
			final List<Chunk> c = fapi.getWarZoneChunks();
			if(!c.isEmpty()) {
				for(int i = 1; i <= amount; i++) {
					final List<Location> cl = getChunkLocations(c.get(random.nextInt(c.size())));
					final EnvoyCrate crate = getRandomCrate(true);
					final Location loc = getRandomLocation(random, cl), newl = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ());
					LivingEnvoyCrate lec = LivingEnvoyCrate.valueOf(newl);
					if(lec == null && crate.canLand(loc)) {
						lec = new LivingEnvoyCrate(totalEnvoys, crate, loc);
						lec.shootFirework();
					} else {
						i -= 1;
					}
				}
			}
		} else if(type.equals("PRESET")) {
			final List<Location> preset = new ArrayList<>(this.preset);
			for(int i = 1; i <= amount; i++) {
				final Location r = preset.get(random.nextInt(preset.size()));
				final World w = r.getWorld();
				final Location newl = new Location(w, r.getBlockX(), r.getBlockY()-1, r.getBlockZ());
				final EnvoyCrate crate = getRandomCrate(true);
				LivingEnvoyCrate lec = LivingEnvoyCrate.valueOf(newl);
				if(lec == null && crate.canLand(r)) {
					lec = new LivingEnvoyCrate(totalEnvoys, crate, r);
					lec.shootFirework();
				} else {
					i -= 1;
				}
				preset.remove(r);
			}
		} else return;
		totalEnvoys += 1;
		scheduler.scheduleSyncDelayedTask(randompackage, () -> stopEnvoy(totalEnvoys, false), 20*despawn);
	}
	public void spawnEnvoy(String summonType, boolean natural, String where) {
		for(String s : config.getStringList("messages.broadcast")) {
			Bukkit.broadcastMessage(translateColorCodes(s.replace("{SUMMON_TYPE}", summonType)));
		}
		spawnEnvoy(where, getRandomAmountSpawned());
		if(natural) {
			scheduler.scheduleSyncDelayedTask(randompackage, () -> spawnEnvoy(translateColorCodes(config.getString("messages.default summon type")), true, where), getRandomTime());
		}
	}
	public void stopEnvoy(int envoyID, boolean dropItems) {
		final HashMap<Integer, HashMap<Location, LivingEnvoyCrate>> L = LivingEnvoyCrate.living;
		if(L != null) {
			final HashMap<Location, LivingEnvoyCrate> l = L.getOrDefault(envoyID, null);
			if(l != null) {
				for(LivingEnvoyCrate c : new ArrayList<>(l.values())) {
					c.delete(dropItems);
				}
			}
		}
	}
	private int getRandomTime() {
		final String r = config.getString("settings.repeats");
		final int min = r.contains("-") ? Integer.parseInt(r.split("-")[0]) : 0, t = r.contains("-") ? min+random.nextInt(Integer.parseInt(r.split("-")[1])-min+1) : Integer.parseInt(r);
		return t*20;
	}
	private Location getRandomLocation(Random random, List<Location> chunklocs) {
		final Location rl = chunklocs.get(random.nextInt(chunklocs.size()));
		final World w = rl.getWorld();
		final int x = rl.getBlockX(), z = rl.getBlockZ();
		return new Location(w, x, w.getHighestBlockYAt(x, z), z);
	}
	private int getRandomAmountSpawned() {
		final String as = config.getString("settings.amount spawned");
		final String[] s = as.split("-");
		final boolean hyphen = as.contains("-");
		final int min = Integer.parseInt(hyphen ? s[0] : as);
		return hyphen ? min+random.nextInt(Integer.parseInt(s[1])-min+1) : min;
	}
	public void viewHelp(CommandSender sender) {
		if(hasPermission(sender, "RandomPackage.envoy.help", true)) {
			sendStringListMessage(sender, config.getStringList("messages.envoy help"), null);
		}
	}
}
