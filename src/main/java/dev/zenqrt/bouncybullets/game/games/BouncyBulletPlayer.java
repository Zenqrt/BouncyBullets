package dev.zenqrt.bouncybullets.game.games;

import org.bukkit.entity.Player;

import java.util.UUID;

public record BouncyBulletPlayer(UUID uuid, Player player, int kills, int deaths, Loadout loadout) {
}
