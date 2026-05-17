package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.SoundUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Objects;

public final class FullHealAbilityItem extends ActiveAbilityItem {

    private static final long COOLDOWN = 1200;
    private static final Sound HEAL_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, Sound.Source.PLAYER, 1, 1.5F);
    private static final Sound DISAPPROVAL_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 0.75F);

    public FullHealAbilityItem() {
        super("healer_active_ability",
                Material.NETHER_STAR,
                AdventureUtils.withoutItalics("Miracle", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text(" (Right Click)", NamedTextColor.GRAY)),
                MiniMessageUtils.wordWrapLore(List.of(
                        "<gray>Upon right-click, fully heal yourself and gain <gold>4❤ <gray>of absorption health.",
                        "",
                        "<dark_gray>Cooldown: <green>" + (COOLDOWN / 20) + "s"
                ), 30)
        );
    }

    @Override
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

        if (player.getHealth() == maxHealth) {
            player.sendMessage(Component.text("Why am I trying to use this right now when I am clearly at full health? Who knows... maybe I am low on brain cells right now.", NamedTextColor.DARK_GRAY));
            player.playSound(DISAPPROVAL_SOUND, Sound.Emitter.self());

            return;
        }

        player.setHealth(maxHealth);
        player.setAbsorptionAmount(4);

        SoundUtils.playSoundFromPlayer(player, HEAL_SOUND);

        Particle.HEART.builder()
                .allPlayers()
                .force(true)
                .location(player.getLocation())
                .count(10)
                .offset(0.5, 2, 0.5)
                .spawn();

        player.setCooldown(event.getMaterial(), (int) COOLDOWN);
    }
}
