package me.randomhashtags.randompackage.addons.utils;

import me.randomhashtags.randompackage.utils.universal.UInventory;

public interface Inventoryable extends Identifiable {
    String getTitle();
    UInventory getInventory();
}
