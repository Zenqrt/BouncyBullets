package dev.zenqrt.bouncybullets.game.games.kit;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.items.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.SoundUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

final class HealerPlayerClass extends EventPlayerClass {

    private static final Gun PRIMARY_GUN = Gun.PISTOL;
    private static final FullHealAbility ACTIVE_ABILITY = new FullHealAbility();

    private final List<BukkitTask> healTasks = new ArrayList<>();

    @Override
    public void registerEvents(BouncyBulletPlayer player) {
        GameItem.registerGameItemEvents(List.of(ACTIVE_ABILITY));
    }

    @Override
    public void onStartUse(BouncyBulletPlayer player) {
        Player playerEntity = player.player();
        Objects.requireNonNull(playerEntity.getAttribute(Attribute.GENERIC_MAX_ABSORPTION)).setBaseValue(5);

        healTasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                if (playerEntity.getGameMode() != GameMode.ADVENTURE) {
                    return;
                }

                double maxHealth = Objects.requireNonNull(playerEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

                if (playerEntity.getHealth() < maxHealth) {
                    playerEntity.setHealth(playerEntity.getHealth() + 1);
                }
            }
        }.runTaskTimer(BouncyBullets.getInstance(), 0, 40));
    }

    @Override
    public void onStopUse(BouncyBulletPlayer player) {
        healTasks.forEach(BukkitTask::cancel);
        healTasks.clear();
    }

    @Override
    public String getName() {
        return "Healer";
    }

    @Override
    public TreeMap<Integer, ItemStack> getItems() {
        return new TreeMap<>() {
            {
                put(0, PRIMARY_GUN.buildItemStack());
                put(1, ACTIVE_ABILITY.buildItemStack());
            }
        };
    }

    private static class FullHealAbility extends ActiveAbilityItem {

        private static final long COOLDOWN = 1200;
        private static final Sound HEAL_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, Sound.Source.PLAYER, 1, 1.5F);
        private static final Sound DISAPPROVAL_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 0.75F);

        FullHealAbility() {
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
}
