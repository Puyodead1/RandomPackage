package me.randomhashtags.randompackage.attribute;

import me.randomhashtags.randompackage.attributesys.EventEntities;
import me.randomhashtags.randompackage.event.DamageEvent;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;

public class SetDamage extends AbstractEventAttribute implements EventEntities {
    @Override
    public void execute(Event event, String value, HashMap<String, String> valueReplacements) {
        if(event instanceof EntityDamageEvent) {
            final EntityDamageEvent e = (EntityDamageEvent) event;
            e.setDamage(evaluate(replaceValue(getEntities(e), value, valueReplacements)));
        } else if(event instanceof DamageEvent) {
            final DamageEvent e = (DamageEvent) event;
            e.setDamage(evaluate(replaceValue(getEntities(e), value, valueReplacements)));
        }
    }
}
