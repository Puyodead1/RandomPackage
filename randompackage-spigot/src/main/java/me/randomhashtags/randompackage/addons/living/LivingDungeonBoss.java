package me.randomhashtags.randompackage.addons.living;

import me.randomhashtags.randompackage.addons.DungeonBoss;
import org.bukkit.Location;

public class LivingDungeonBoss {
    private DungeonBoss type;
    private Location l;
    public LivingDungeonBoss(DungeonBoss type, Location l) {
        this.type = type;
        this.l = l;
    }
    public DungeonBoss getType() { return type; }
    public Location getSpawnLocation() { return l; }
}
