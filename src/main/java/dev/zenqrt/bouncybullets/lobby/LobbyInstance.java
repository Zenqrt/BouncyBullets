package dev.zenqrt.bouncybullets.lobby;

import dev.zenqrt.bouncybullets.sidebar.sidebars.LobbySidebar;
import dev.zenqrt.bouncybullets.stats.PlayerStats;
import dev.zenqrt.bouncybullets.utils.NMSConverter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LobbyInstance {

    private final Map<UUID, LobbySidebar> sidebarMap = new HashMap<>();

    private final Location spawn;

    public LobbyInstance(Location spawn) {
        this.spawn = spawn;
    }

    public void join(Player player, PlayerStats stats, boolean teleport) {
        player.getInventory().clear();

        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.clearActivePotionEffects();

        LobbySidebar sidebar = new LobbySidebar(stats);
        sidebar.addViewer(NMSConverter.serverPlayer(player));

        this.sidebarMap.put(player.getUniqueId(), sidebar);

        if (teleport) {
            player.teleport(this.spawn);
        }
    }

    public void leave(Player player) {
        LobbySidebar existingSidebar = this.sidebarMap.remove(player.getUniqueId());

        if (existingSidebar != null)
            existingSidebar.removeViewer(NMSConverter.serverPlayer(player));
    }

    public Location getSpawn() {
        return spawn;
    }
}
