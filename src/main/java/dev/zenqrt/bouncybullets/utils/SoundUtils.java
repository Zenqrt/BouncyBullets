package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class SoundUtils {

    public static void playSoundFromPlayer(Player source, Sound sound) {
        Location location = source.getLocation();

        for (Player other : source.getWorld().getPlayers()) {
            other.playSound(
                    sound,
                    location.getX(),
                    location.getY(),
                    location.getZ()
            );
        }

        source.playSound(sound, Sound.Emitter.self());
    }

}
