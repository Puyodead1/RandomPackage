package me.randomhashtags.randompackage.api;

import me.randomhashtags.randompackage.RandomPackage;
import me.randomhashtags.randompackage.RandomPackageAPI;
import me.randomhashtags.randompackage.api.events.coinflip.CoinFlipEndEvent;
import me.randomhashtags.randompackage.api.events.customenchant.*;
import me.randomhashtags.randompackage.api.events.fund.FundDepositEvent;
import me.randomhashtags.randompackage.api.events.globalchallenges.GlobalChallengeParticipateEvent;
import me.randomhashtags.randompackage.utils.RPPlayer;
import me.randomhashtags.randompackage.utils.classes.customenchants.CustomEnchant;
import me.randomhashtags.randompackage.utils.classes.customenchants.EnchantRarity;
import me.randomhashtags.randompackage.utils.classes.globalchallenges.ActiveGlobalChallenge;
import me.randomhashtags.randompackage.utils.classes.globalchallenges.GlobalChallenge;
import me.randomhashtags.randompackage.utils.classes.globalchallenges.GlobalChallengePrize;
import me.randomhashtags.randompackage.utils.supported.MCMMOAPI;
import me.randomhashtags.randompackage.utils.universal.UInventory;
import me.randomhashtags.randompackage.utils.universal.UMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class GlobalChallenges extends RandomPackageAPI implements CommandExecutor, Listener {

	public boolean isEnabled = false;
	private static GlobalChallenges instance;
	public static final GlobalChallenges getChallenges() {
		if(instance == null) instance = new GlobalChallenges();
		return instance;
	}
	
	public YamlConfiguration config;
	private UInventory inv, leaderboard, claimPrizes;
	private int topPlayersSize = 54;

	private File dataF;
	private YamlConfiguration data;

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		final Player player = sender instanceof Player ? (Player) sender : null;
		if(args.length == 0 && player != null)
		    viewCurrent(player);
		else if(args.length >= 2 && args[0].equals("stop")) {
			if(hasPermission(player, "RandomPackage.globalchallenges.stop", true))
				stopChallenge(GlobalChallenge.challenges.get(args[1].replace("_", " ")), false);
		} else if(player != null && args.length >= 1 && args[0].equals("claim"))
			viewPrizes(player);
		else if(args.length >= 1 && args[0].equals("reload")) {
			if(hasPermission(sender, "RandomPackage.globalchallenges.reload", true))
				reloadChallenges();
		} else if(args.length >= 3 && args[0].equals("giveprize")) {
			if(hasPermission(sender, "RandomPackage.globalchallenges.giveprize", true)) {
				final OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
				final int placing = getRemainingInt(args[2]);
				if(op != null && placing != -1) {
					RPPlayer.get(op.getUniqueId()).addGlobalChallengePrize(GlobalChallengePrize.valueOf(placing));
				}
			}
		}
		return true;
	}

	public void enable() {
	    final long started = System.currentTimeMillis();
		if(isEnabled) return;
		save(null, "global challenges.yml");
		save("_Data", "global challenges.yml");
		pluginmanager.registerEvents(this, randompackage);
		isEnabled = true;
		if(RandomPackage.mcmmo != null) {
			final MCMMOAPI m = MCMMOAPI.getMCMMOAPI();
			m.gcIsEnabled = true;
			m.enable();
		}
		config = YamlConfiguration.loadConfiguration(new File(rpd, "global challenges.yml"));
		dataF = new File(rpd + separator + "_Data", "global challenges.yml");
		data = YamlConfiguration.loadConfiguration(dataF);

		if(!otherdata.getBoolean("saved default global challenges")) {
			final String[] c = new String[]{
					"AGGRESSIVE_MOBS_KILLED", "ALCHEMIST_EXCHANGES", "ALL_ORES_MINED",
					"BIRCH_LOGS_CUT", "BLOCKS_MINED_BY_PICKAXE", "BLOCKS_PLACED",
					"COINFLIPS_WON", "CUSTOM_ENCHANTS_REVEALED",
					"DIAMOND_ORE_MINED",
					"EMERALD_ORE_MINED", "END_MOBS_KILLED", "ENVOY_CHESTS_LOOTED", "EXP_GAINED",
					"FISH_CAUGHT",
					"GOLD_ORE_MINED",
					"JACKPOT_MONEY_SPENT", "JACKPOT_TICKETS_BOUGHT",
					"LAPIS_ORE_MINED",
					"MCMMO_XP_GAINED_IN_ACROBATICS", "MCMMO_XP_GAINED_IN_SWORDS", "MCMMO_XP_GAINED_IN_UNARMED",
                    "MOBS_KILLED",
                    "MONEY_LOST_IN_COINFLIPS", "MONEY_WON_IN_COINFLIPS",
					"PASSIVE_MOBS_KILLED", "PVP_DAMAGE",
					"RANKED_DUEL_WINS", "REDSTONE_ORE_MINED",
					"TIME_SPENT_IN_END", "TIME_SPENT_IN_MAIN_WARZONE",
					"UNIQUE_PLAYER_HEADS_COLLECTED", "UNIQUE_PLAYER_KILLS"
			};
			for(String s : c) save("global challenges", s + ".yml");
			otherdata.set("saved default global challenges", true);
			saveOtherData();
		}

		for(File f : new File(rpd + separator + "global challenges").listFiles()) {
			new GlobalChallenge(f, new ArrayList<>());
		}

		inv = new UInventory(null, config.getInt("gui.size"), ChatColor.translateAlternateColorCodes('&', config.getString("gui.title")));
		topPlayersSize = config.getInt("challenge leaderboard.how many");
		leaderboard = new UInventory(null, ((topPlayersSize+9)/9)*9, ChatColor.translateAlternateColorCodes('&', config.getString("challenge leaderboard.title")));
		claimPrizes = new UInventory(null, 9, ChatColor.translateAlternateColorCodes('&', config.getString("rewards.title")));

		for(String s : config.getConfigurationSection("rewards").getKeys(false)) {
			if(!s.equals("title")) {
				new GlobalChallengePrize(d(config, "rewards." + s + ".prize"), config.getInt("rewards." + s + ".amount"), Integer.parseInt(s), config.getStringList("rewards." + s + ".prizes"));
			}
		}

		final TreeMap<String, GlobalChallenge> G = GlobalChallenge.challenges;
		final List<GlobalChallengePrize> P = GlobalChallengePrize.prizes;
		sendConsoleMessage("&6[RandomPackage] &aLoaded " + (G != null ? G.size() : 0) + " global challenges and " + (P != null ? P.size() : 0) + " prizes &e(took " + (System.currentTimeMillis()-started) + "ms)");
		reloadChallenges();
	}
	public void disable() {
		if(!isEnabled) return;
		isEnabled = false;
		data.set("active global challenges", null);
		for(ActiveGlobalChallenge c : ActiveGlobalChallenge.active.values()) {
			final String p = c.getType().getYamlName();
			data.set("active global challenges." + p + ".started", c.getStartedTime());
			final HashMap<UUID, Double> participants = c.getParticipants();
			for(UUID u : participants.keySet())
				data.set("active global challenges." + p + ".participants." + u, participants.get(u));
		}

		try {
			data.save(dataF);
			dataF = new File(rpd + separator + "_Data", "global challenges.yml");
			data = YamlConfiguration.loadConfiguration(dataF);
		} catch (Exception e) {
			e.printStackTrace();
		}

		GlobalChallenge.deleteAll();
		GlobalChallengePrize.deleteAll();
		HandlerList.unregisterAll(this);
	}
	public void reloadChallenges() {
		final int max = config.getInt("challenge settings.max at once");
		int maxAtOnce = max;
		final TreeMap<String, GlobalChallenge> gc = GlobalChallenge.challenges;
		final ConfigurationSection EEE = data.getConfigurationSection("active global challenges");
		if(gc != null && EEE != null) {
		    final long started = System.currentTimeMillis();
			int loadeD = 0;
			for(String s : EEE.getKeys(false)) {
				if(maxAtOnce > 0) {
					loadeD += 1;
					final GlobalChallenge g = gc.getOrDefault(s, null);
					if(g != null) {
						final HashMap<UUID, Double> participants = new HashMap<>();
						final ConfigurationSection partic = data.getConfigurationSection("active global challenges." + s + ".participants");
						if(partic != null) {
							for(String u : partic.getKeys(false)) {
								final UUID uuid = UUID.fromString(u);
								participants.put(uuid, data.getDouble("active global challenges." + s + ".participants." + u));
							}
						}
						maxAtOnce -= 1;
						g.start(Long.parseLong(data.getString("active global challenges." + s + ".started")), participants);
					}
				}
			}
			sendConsoleMessage("&6[RandomPackage] &aStarted " + loadeD + " pre-existing global challenges &e(took " + (System.currentTimeMillis()-started) + "ms)");
		}
		if(maxAtOnce > 0) {
			final long started = System.currentTimeMillis();
			for(int i = 1; i <= maxAtOnce; i++) {
				final GlobalChallenge r = getRandomChallenge();
				if(!r.isActive()) r.start();
				else i-=1;
			}
			sendConsoleMessage("&6[RandomPackage] &aStarted " + maxAtOnce + " new global challenges &e(took " + (System.currentTimeMillis()-started) + "ms)");
		}
		reloadInventory();
	}

	public void reloadInventory() {
		final Inventory inv = this.inv.getInventory();
		int f = 0;
		for(int i = 0; i < inv.getSize(); i++) {
			if(config.get("gui." + i) != null) {
				final String p = config.getString("gui." + i + ".item");
				if(p.toUpperCase().equals("{CHALLENGE}")) {
					ActiveGlobalChallenge z = f < ActiveGlobalChallenge.active.size() ? (ActiveGlobalChallenge) ActiveGlobalChallenge.active.values().toArray()[f] : null;
					if(z == null) z = getRandomChallenge().start();
					final GlobalChallenge T = z.getType();
					final String n = T.getType();
					item = T.getDisplayItem().clone();
					itemMeta = item.getItemMeta(); lore.clear();
					for(String s : config.getStringList("challenge settings.added lore")) {
						lore.add(ChatColor.translateAlternateColorCodes('&', s.replace("{TYPE}", n)));
					}
					itemMeta.setLore(lore); lore.clear();
					item.setItemMeta(itemMeta);
					f += 1;
				} else {
					item = d(config, "gui." + i);
				}
				inv.setItem(i, item);
			}
		}
	}


	public void viewPrizes(Player player) {
		if(hasPermission(player, "RandomPackage.globalchallenges.claim", true)) {
			final HashMap<GlobalChallengePrize, Integer> prizes = RPPlayer.get(player.getUniqueId()).getGlobalChallengePrizes();
			int size = (prizes.size()/9)*9;
			size = size == 0 ? 9 : size > 54 ? 54 : size;
			player.openInventory(Bukkit.createInventory(player, size, claimPrizes.getTitle()));
			final Inventory top = player.getOpenInventory().getTopInventory();
			player.updateInventory();
			for(GlobalChallengePrize prize : prizes.keySet()) {
				item = prize.getDisplay(); item.setAmount(prizes.get(prize));
				top.addItem(item);
			}
		}
	}
	public void givePrize(UUID player, GlobalChallengePrize prize, boolean sendMessage) {
		final OfflinePlayer op = Bukkit.getOfflinePlayer(player);
		if(op != null && op.isOnline()) {
			final Player p = op.getPlayer();
			final HashMap<String, ItemStack> rewards = prize.getRandomRewards();
			for(String s : rewards.keySet()) {
				giveItem(p, d(null, s));
			}
			if(sendMessage) {
				final String placing = prize.getPlacement() + "";
				for(String s : config.getStringList("messages.claimed prize"))
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', s.replace("{PLACING}", placing)));
			}
		}
	}
	public Map<UUID, Double> getPlacing(HashMap<UUID, Double> participants) {
		return participants.entrySet().stream().sorted(Map.Entry.<UUID, Double> comparingByValue().reversed()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	public Map<UUID, Double> getPlacing(HashMap<UUID, Double> participants, int returnFirst) {
		final HashMap<UUID, Double> a = new HashMap<>();
		final HashMap<UUID, Double> d = participants.entrySet().stream().sorted(Map.Entry.<UUID, Double> comparingByValue().reversed()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		for(int i = 1; i <= returnFirst && i-1 < d.size(); i++) {
			a.put((UUID) d.keySet().toArray()[i-1], (Double) d.values().toArray()[i-1]);
		}
		return a;
	}
	public int getRanking(UUID player, ActiveGlobalChallenge g) {
		final Map<UUID, Double> byValue = g.getParticipants().entrySet().stream().sorted(Map.Entry.<UUID, Double> comparingByValue().reversed()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		int placement = indexOf(byValue.keySet(), player);
		placement = placement == -1 ? byValue.keySet().size() : placement+1;
		return placement;
	}
	public String getRanking(int rank) {
		String ranking = formatInt(rank);
		ranking = ranking + (ranking.endsWith("1") ? "st" : ranking.endsWith("2") ? "nd" : ranking.endsWith("3") ? "rd" : ranking.equals("0") ? "" : "th");
		return ranking;
	}
	public HashMap<Integer, UUID> getRankings(ActiveGlobalChallenge g) {
		final List<UUID> participants = new ArrayList<>(g.getParticipants().keySet());
		final HashMap<Integer, UUID> rankings = new HashMap<>();
		for(UUID u : participants)
			rankings.put(getRanking(u, g), u);
		return rankings;
	}
	public void viewCurrent(Player player) {
		if(hasPermission(player, "RandomPackage.globalchallenges", true)) {
			final UUID u = player.getUniqueId();
			player.openInventory(Bukkit.createInventory(player, inv.getSize(), inv.getTitle()));
			final Inventory top = player.getOpenInventory().getTopInventory();
			top.setContents(inv.getInventory().getContents());
			player.updateInventory();
			for(int i = 0; i < top.getSize(); i++) {
				item = top.getItem(i);
				if(item != null) {
					final ActiveGlobalChallenge g = ActiveGlobalChallenge.valueOf(item);
					if(g != null) {
						final HashMap<UUID, Double> participants = g.getParticipants();
						final Map<UUID, Double> placings = getPlacing(participants);
						int topp = 0;
						UUID ranked = topp < placings.size() ? (UUID) placings.keySet().toArray()[topp] : null;
						final String remainingtime = getRemainingTime(g.getRemainingTime());
						itemMeta = item.getItemMeta(); lore.clear();
						if(item.hasItemMeta()) {
							if(itemMeta.hasLore()) {
								final String ranking = getRanking(getRanking(u, g)), date = toReadableDate(new Date(g.getStartedTime()), config.getString("challenge settings.date format")), v = formatDouble(g.getValue(u));
								for(String s : itemMeta.getLore()) {
									s = s.replace("{DATE}", date).replace("{YOUR_VALUE}", v).replace("{YOUR_RANKING}", ranking).replace("{TIME_LEFT}", remainingtime);
									if(s.contains("{TOP}")) {
										if(ranked == null) {
											s = s.replace("{TOP}", "None").replace("{VALUE}", "0");
										} else {
											s = s.replace("{TOP}", Bukkit.getOfflinePlayer(ranked).getName());
											if(s.contains("{VALUE}")) {
												final double d = participants.getOrDefault(ranked, 0.00);
												s = s.replace("{VALUE}", d > 0.00 ? formatDouble(d) : "0");
											}
										}
										topp += 1;
										ranked = topp < placings.size() ? (UUID) placings.keySet().toArray()[topp] : null;
									}
									lore.add(s);
								}
							}
							itemMeta.setLore(lore); lore.clear();
							item.setItemMeta(itemMeta);
						}
					}
				}
			}
			player.updateInventory();
		}
	}
	public void stopChallenge(GlobalChallenge chall, boolean giveRewards) {
		final HashMap<GlobalChallenge, ActiveGlobalChallenge> a = ActiveGlobalChallenge.active;
		if(a != null && a.containsKey(chall)) {
			a.get(chall).end(giveRewards, 3);
		}
	}
	public void viewTopPlayers(Player player, ActiveGlobalChallenge active) {
		player.closeInventory();
		player.openInventory(Bukkit.createInventory(player, leaderboard.getSize(), leaderboard.getTitle()));
		final Inventory top = player.getOpenInventory().getTopInventory();
		final String n = ChatColor.translateAlternateColorCodes('&', config.getString("challenge leaderboard.name")), N = active.getType().getType();
		final HashMap<Integer, UUID> rankings = getRankings(active);
		final List<String> a = config.getStringList("challenge leaderboard.lore");
		item = UMaterial.PLAYER_HEAD_ITEM.getItemStack();
		for(int i = 0; i < topPlayersSize && i < rankings.size(); i++) {
			final UUID u = rankings.get(i+1);
			final OfflinePlayer OP = Bukkit.getOfflinePlayer(u);
			final String ranking = getRanking(i+1), name = OP.getName(), value = formatDouble(active.getValue(u));
			item.setAmount(i+1);
			final SkullMeta skm = (SkullMeta) item.getItemMeta();
			skm.setDisplayName(n.replace("{PLAYER}", name));
			skm.setOwner(name); lore.clear();
			for(String s : a) {
				lore.add(ChatColor.translateAlternateColorCodes('&', s.replace("{RANKING}", ranking).replace("{CHALLENGE}", N).replace("{VALUE}", value)));
			}
			skm.setLore(lore); lore.clear();
			item.setItemMeta(skm);
			top.setItem(i, item);
		}
		player.updateInventory();
	}
	public GlobalChallenge getRandomChallenge() {
		final ArrayList<GlobalChallenge> g = new ArrayList<>(GlobalChallenge.challenges.values());
		return g.get(random.nextInt(g.size()));
	}
	
	@EventHandler
	private void inventoryClickEvent(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();
		final Inventory top = player.getOpenInventory().getTopInventory();
		if(!event.isCancelled() && player == top.getHolder()) {
			final String t = event.getView().getTitle();
			if(t.equals(inv.getTitle()) || t.equals(leaderboard.getTitle()) || t.equals(claimPrizes.getTitle())) {
				event.setCancelled(true);
				player.updateInventory();

				final int r = event.getRawSlot();
				final ItemStack c = event.getCurrentItem();
				
				if(r >= top.getSize() || r < 0 || !event.getClick().equals(ClickType.LEFT) && !event.getClick().equals(ClickType.RIGHT) || c == null) return;
				if(t.equals(inv.getTitle())) {
					final ActiveGlobalChallenge g = ActiveGlobalChallenge.valueOf(c);
					if(g != null) {
						player.closeInventory();
						viewTopPlayers(player, g);
					}
				} else if(t.equals(claimPrizes.getTitle())) {
					final GlobalChallengePrize prize = GlobalChallengePrize.valueOf(c);
					givePrize(player.getUniqueId(), prize, true);
					item = c.clone();
					item = item.getAmount() == 1 ? new ItemStack(Material.AIR) : item;
					top.setItem(r, item);
				}
				player.updateInventory();
			}
		}
	}
	public void increase(Event event, String input, UUID player, double increaseBy) {
		input = input.toLowerCase();
		for(ActiveGlobalChallenge g : ActiveGlobalChallenge.active.values()) {
			final String t = g.getType().getTracks().split(";")[0].toLowerCase();
			if(t.equals(input)
					|| t.startsWith("blocksminedbymaterial_") && input.startsWith("blocksminedbymaterial_") && input.split("blocksminedbymaterial_")[1].endsWith(t.split("blocksminedbymaterial_")[1])
					|| t.startsWith("customenchantmprocs_") && input.startsWith("customenchantmprocs_") && input.split("customenchantmprocs_")[1].endsWith(t.split("customenchantmprocs")[1])) {
				final GlobalChallengeParticipateEvent e = new GlobalChallengeParticipateEvent(g, event, input, increaseBy);
				pluginmanager.callEvent(e);
				if(!e.isCancelled()) {
					g.increaseValue(player, e.value);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if(!event.isCancelled()) {
			final UUID damager = event.getDamager().getUniqueId();
			if(event.getDamager() instanceof Player) {
				final double dmg = event.getFinalDamage();
				increase(event, "pvadamage", damager, dmg);
				final Entity e = event.getEntity();
				if(e instanceof Player) increase(event, "pvpdamage", damager, dmg);
				else if(e instanceof LivingEntity) increase(event, "pvedamage", damager, dmg);
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void entityDeathEvent(EntityDeathEvent event) {
		final LivingEntity e = event.getEntity();
		final UUID u = e.getUniqueId();
		if(e instanceof Player) {
			increase(event, "playerdeaths", u, 1);
		} else {
		    final Player killer = e.getKiller();
            if(killer != null) {
                final UUID k = killer.getUniqueId();
                if(e instanceof Monster) increase(event, "hostilemobskilled", k, 1);
                else increase(event, "passivemobskilled", k, 1);
                increase(event, "mobskilled", k, 1);
            }
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void blockPlaceEvent(BlockPlaceEvent event) {
		if(!event.isCancelled()) {
			final UUID player = event.getPlayer().getUniqueId();
			increase(event, "blocksplaced", player, 1);
			final Block b = event.getBlock();
			increase(event, b.getType().name() + ":" + b.getData() + "_placed", player, 1);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void blockBreakEvent(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			final Player player = event.getPlayer();
			final UUID u = player.getUniqueId();
			increase(event, "blocksmined", u, 1);
			final Block b = event.getBlock();
			increase(event, b.getType().name() + ":" + b.getData() + "_mined", u, 1);
			final String m = getItemInHand(player).getType().name();
			increase(event, "blocksminedbymaterial_" + m, u, 1);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void playerFishEvent(PlayerFishEvent event) {
		final State s = event.getState();
		final UUID player = event.getPlayer().getUniqueId();
		if(s.equals(State.CAUGHT_FISH)) {
			increase(event, "fishcaught", player, 1);
			if(event.getCaught() instanceof Fish) increase(event, event.getCaught().getType().name() + "_Caught", player, 1);
		} else if(s.equals(State.CAUGHT_ENTITY)) {
			increase(event, "treasurecaught", player, 1);
			if(event.getCaught() instanceof Item) {
				final ItemStack i = ((Item) event.getCaught()).getItemStack();
				increase(event, i.getType().name() + ":" + i.getData().getData() + "_caught", player, 1);
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void playerExpChangeEvent(PlayerExpChangeEvent event) {
		if(event.getAmount() > 0) {
			increase(event, "expgained", event.getPlayer().getUniqueId(), event.getAmount());
		}
	}
	/*
	 * RandomPackage Events
	 */
	@EventHandler
	private void alchemistExchangeEvent(AlchemistExchangeEvent event) {
		if(!event.isCancelled()) {
			increase(event, "alchemistexchanges", event.player.getUniqueId(), 1);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void playerRevealCustomEnchantEvent(PlayerRevealCustomEnchantEvent event) {
		if(!event.isCancelled()) {
			final UUID player = event.player.getUniqueId();
			increase(event, "customenchantsrevealed", player, 1);
			increase(event, "customenchantsrevealed_" + EnchantRarity.valueOf(event.enchant).getName(), player, 1);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void coinFlipChallengeEndEvent(CoinFlipEndEvent event) {
		final UUID winner = event.winner, loser = event.loser;
		final long wager = event.wager, total = (long) (wager*2*event.tax);
		increase(event, "coinflipswon", winner, 1);
		increase(event, "coinflipslost", loser, 1);
		increase(event, "$wonincoinflip", winner, total);
		increase(event, "$lostincoinflip", loser, total);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void fundDepositEvent(FundDepositEvent event) {
		if(!event.isCancelled()) {
			increase(event, "$funddeposited", event.player.getUniqueId(), event.amount);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void customEnchantProcEvent(CustomEnchantProcEvent event) {
		if(event.player != null && !event.isCancelled()) {
			final UUID player = event.player.getUniqueId();
			final CustomEnchant enchant = event.enchant;
			increase(event, "customenchantprocs", player, 1);
			increase(event, "customenchantprocs_" + EnchantRarity.valueOf(enchant).getName(), player, 1);
			increase(event, "customenchantproc'd_" + enchant.getYamlName(), player, 1);
			final ItemStack i = event.itemWithEnchant;
			increase(event, "customenchantmprocs_" + i.getType().name() + ":" + i.getData().getData(), player, 1);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void customEnchantApplyEvent(PlayerApplyCustomEnchantEvent event) {
		final UUID player = event.player.getUniqueId();
		increase(event, "customenchantsapplied", player, 1);
		increase(event, "customenchantsapplied_" + EnchantRarity.valueOf(event.enchant).getName(), player, 1);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void enchanterPurchaseEvent(EnchanterPurchaseEvent event) {
		if(!event.isCancelled()) {
			final UUID player = event.getPlayer().getUniqueId();
			increase(event, "enchanterpurchases", player, 1);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	private void tinkererTradeEvent(TinkererTradeEvent event) {
		if(!event.isCancelled()) {
			final UUID player = event.player.getUniqueId();
			increase(event, "tinkerertrades", player, 1);
			increase(event, "tinkereritemtrades", player, event.trades.size());
		}
	}
}
