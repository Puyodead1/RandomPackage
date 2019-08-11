package me.randomhashtags.randompackage.utils.supported.regional;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRenameEvent;
import com.massivecraft.factions.struct.Relation;
import me.randomhashtags.randompackage.utils.RPFeature;
import me.randomhashtags.randompackage.utils.supported.Regional;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FactionsUUID extends RPFeature implements Regional {
    private static FactionsUUID instance;
    public static FactionsUUID getFactionsUUID() {
        if(instance == null) instance = new FactionsUUID();
        return instance;
    }

    private FPlayers fi;
    private Factions f;
    private Board b;
    private HashMap<String, HashMap<String, List<UUID>>> relations;

    public String getIdentifier() { return "REGIONAL_FACTIONS_UUID"; }
    public void load() {
        fi = FPlayers.getInstance();
        f = Factions.getInstance();
        b = Board.getInstance();
        relations = new HashMap<>();
    }
    public void unload() {
        fi = null;
        f = null;
        b = null;
        relations = null;
        instance = null;
    }

    private Faction getFaction(UUID player) {
        final FPlayer fp = getFPlayer(player);
        return fp != null ? fp.getFaction() : null;
    }

    private FPlayer getFPlayer(UUID uuid) { return fi.getByOfflinePlayer(Bukkit.getOfflinePlayer(uuid)); }
    public ChatColor getRelationColor(OfflinePlayer player, Player target) {
        final Faction f = getFaction(player.getUniqueId());
        return f != null ? f.getColorTo(getFPlayer(target.getUniqueId())) : null;
    }

    private List<UUID> getMembers(UUID player, String TYPE) {
        final Faction f = getFaction(player);
        final String faction = f != null ? f.getTag() : null;
        if(faction == null) return new ArrayList<>();
        if(!relations.containsKey(faction)) relations.put(faction, new HashMap<>());
        if(relations.get(faction).containsKey(TYPE)) {
            return relations.get(faction).get(TYPE);
        } else {
            final List<UUID> members = new ArrayList<>();
            for(FPlayer fp : fi.getAllFPlayers()) {
                final Relation t = fp.getRelationTo(f);
                if(TYPE.equals("MEMBERS") && t.isMember()
                        || TYPE.equals("ENEMIES") && t.isEnemy()
                        || TYPE.equals("ALLIES") && t.isAlly()
                        || TYPE.equals("TRUCES") && t.isTruce()
                        || TYPE.equals("NEUTRAL") && t.isNeutral()
                )
                    members.add(fp.getPlayer().getUniqueId());
            }
            relations.get(faction).put(TYPE, members);
            return members;
        }
    }
    public List<UUID> getAssociates(UUID player) { return getMembers(player, "MEMBERS"); }
    public List<UUID> getNeutrals(UUID player) { return getMembers(player, "NEUTRAL"); }
    public List<UUID> getAllies(UUID player) { return getMembers(player, "ALLIES"); }
    public List<UUID> getTruces(UUID player) { return getMembers(player, "TRUCES"); }
    public List<UUID> getEnemies(UUID player) { return getMembers(player, "ENEMIES"); }

    public boolean canModify(UUID player, Location blockLocation) {
        final Faction p = getFPlayer(player).getFaction(), f = b.getFactionAt(new FLocation(blockLocation));
        return f.isWilderness() || p != null && p.equals(f);
    }

    public List<Player> getOnlineAssociates(UUID player) {
        final Faction f = getFaction(player);
        return f != null ? f.getOnlinePlayers() : new ArrayList<>();
    }

    public List<Chunk> getChunks(String regionalIdentifier) {
        final Faction faction = f.getFactionById(regionalIdentifier);
        final List<Chunk> a = new ArrayList<>();
        if(faction != null) {
            for(FLocation l : faction.getAllClaims()) {
                final Chunk c = l.getWorld().getChunkAt((int) l.getX(), (int) l.getZ());
                if(!a.contains(c)) a.add(c);
            }
        }
        return a;
    }

    public String getRole(UUID player) { return getFPlayer(player).getRole().getPrefix(); }
    public String getRegionalIdentifier(UUID player) {
        final Faction f = getFaction(player);
        return f != null ? f.getTag() : null;
    }
    public String getRegionalIdentifierAt(Location l) { return b.getFactionAt(new FLocation(l)).getTag(); }
    public String getChatMode(UUID player) { return getFPlayer(player).getChatMode().name(); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void factionDisbandEvent(FactionDisbandEvent event) {
        pluginmanager.callEvent(new me.randomhashtags.randompackage.events.FactionDisbandEvent(event.getPlayer(), event.getFaction().getTag()));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void factionRenameEvent(FactionRenameEvent event) {
        pluginmanager.callEvent(new me.randomhashtags.randompackage.events.FactionRenameEvent(event.getPlayer(), event.getOldFactionTag(), event.getFactionTag()));
    }
}
