package me.randomhashtags.randompackage.api.unfinished;

import me.randomhashtags.randompackage.RandomPackageAPI;
import me.randomhashtags.randompackage.utils.classes.Pet;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.HashMap;

public class Pets extends RandomPackageAPI implements Listener {

    private static Pets instance;
    public static final Pets getPets() {
        if(instance == null) instance = new Pets();
        return instance;
    }

    public boolean isEnabled = false;
    public YamlConfiguration config;

    public void enable() {
        final long started = System.currentTimeMillis();
        if(isEnabled) return;
        isEnabled = true;
        pluginmanager.registerEvents(this, randompackage);

        if(!otherdata.getBoolean("saved default pets")) {
            final String[] p = new String[] {"ANTI_TELEBLOCK", "BANNER", "LAVA_ELEMENTAL", "WATER_ELEMENTAL"};
            for(String s : p) save("pets", s + ".yml");
            otherdata.set("saved default pets", true);
            saveOtherData();
        }

        for(File f : new File(rpd + separator + "pets").listFiles()) {
            new Pet(f);
        }

        final HashMap<String, Pet> p = Pet.pets;
        sendConsoleMessage("&6[RandomPackage] &aLoaded " + (p != null ? p.size() : 0) + " Pets &e(took " + (System.currentTimeMillis()-started) + "ms)");
    }
    public void disable() {
        if(!isEnabled) return;
        isEnabled = false;
        config = null;
        Pet.deleteAll();
        HandlerList.unregisterAll(this);
    }


}
