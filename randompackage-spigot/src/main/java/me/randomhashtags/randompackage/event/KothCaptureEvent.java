package me.randomhashtags.randompackage.event;

import org.bukkit.entity.Player;

public final class KothCaptureEvent extends RPEventCancellable {
    public KothCaptureEvent(Player player) {
        super(player);
    }
}
