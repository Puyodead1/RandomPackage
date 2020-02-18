package me.randomhashtags.randompackage.addon.living;

import me.randomhashtags.randompackage.addon.obj.CustomEnchantEntity;
import me.randomhashtags.randompackage.addon.util.Mathable;
import me.randomhashtags.randompackage.api.CustomEnchants;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.UUID;

public class LivingCustomEnchantEntity extends CustomEnchants implements Mathable {
    public static HashMap<UUID, LivingCustomEnchantEntity> living;
    private CustomEnchantEntity type;
    private boolean isCreature;
    private LivingEntity summoner, entity, target;
    private UUID uuid;

    public LivingCustomEnchantEntity(CustomEnchantEntity type, LivingEntity summoner, LivingEntity entity, LivingEntity target) {
        if(living == null) {
            living = new HashMap<>();
        }
        this.type = type;
        this.summoner = summoner;
        this.entity = entity;
        isCreature = entity instanceof Creature;
        uuid = entity.getUniqueId();
        this.target = target;

        entity.setCustomName(type.getCustomName().replace("{PLAYER}", summoner.getName()));
        entity.setCanPickupItems(false);
        entity.setCustomNameVisible(true);
        if(isCreature && target != null) {
            ((Creature) entity).setTarget(target);
        }
        for(String s : type.getAttributes()) {
            int b = -1;
            for(String attr : s.split(";")) {
                b += 1;
                //ce.w(null, event, null, Arrays.asList(entity), attr, s, b, summoner instanceof Player ? (Player) summoner : null);
                if(attr.toLowerCase().startsWith("despawn{")) {
                    SCHEDULER.scheduleSyncDelayedTask(RANDOM_PACKAGE, () -> entity.remove(), (int) evaluate(attr.split("\\{")[1].split("}")[0]));
                }
            }
        }
        living.put(uuid, this);
    }

    public CustomEnchantEntity getType() {
        return type;
    }
    public LivingEntity getSummoner() {
        return summoner;
    }
    public LivingEntity getEntity() {
        return entity;
    }
    public LivingEntity getTarget() {
        return target;
    }
    public void setTarget(LivingEntity target) {
        this.target = target;
        if(isCreature && (type.canTargetSummoner() || target != summoner)) {
            ((Creature) entity).setTarget(target);
        }
    }
    public void setSummoner(LivingEntity summoner) {
        this.summoner = summoner;
    }

    public void delete(boolean remove) {
        living.remove(uuid);
        if(remove) {
            entity.remove();
        }
        if(living.isEmpty()) {
            living = null;
        }
    }
    public static LivingCustomEnchantEntity valueOf(LivingEntity summoner) {
        for(LivingCustomEnchantEntity l : living.values()) {
            if(l.summoner.equals(summoner)) {
                return l;
            }
        }
        return null;
    }
}
