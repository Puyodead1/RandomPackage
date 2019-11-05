package me.randomhashtags.randompackage.supported.economy;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class Vault {
    private static Vault instance;
    public static Vault getVault() {
        if(instance == null) instance = new Vault();
        return instance;
    }
    private boolean didSetupEco = false;
    private Economy economy = null;
    public Chat chat = null;
    public static Permission permissions = null;
    public boolean setupEconomy() {
        didSetupEco = true;
        final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(economyProvider != null) economy = economyProvider.getProvider();
        return economy != null;
    }
    public Economy getEconomy() {
        if(!didSetupEco) setupEconomy();
        return economy;
    }
    public boolean setupChat() {
        final RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp != null ? rsp.getProvider() : null;
        return chat != null;
    }
    public boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        permissions = rsp != null ? rsp.getProvider() : null;
        return permissions != null;
    }
}
