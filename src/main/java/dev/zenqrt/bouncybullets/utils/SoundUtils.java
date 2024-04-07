package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public final class SoundUtils {

    public static void playSoundFromPlayer(Player source, Sound sound) {
        Audience.audience(source.getWorld().getPlayers().stream()
                .filter(player -> player != source)
                .toList()).playSound(sound, source.getLocation().getX(), source.getLocation().getY(), source.getLocation().getZ());

        source.playSound(sound, Sound.Emitter.self());
    }

}
