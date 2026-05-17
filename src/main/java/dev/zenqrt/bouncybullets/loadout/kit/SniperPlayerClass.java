package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.PaperEventListener;
import dev.zenqrt.bouncybullets.event.events.GunShootEvent;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.GlowUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

import java.util.*;

// TODO: Change active ability to marking player and dealing more damage to them for a short period of time
// TODO: Ability: Make every player glowing and the next shot does twice as much damage
final class SniperPlayerClass extends EventPlayerClass {

    private static final GunItem PRIMARY_GUN = GameItems.SNIPER_RIFLE;
    private static final GunItem SECONDARY_GUN = GameItems.PISTOL;

    private final Map<UUID, Long> lastMoved = new HashMap<>();
    private final List<BukkitTask> tasks = new ArrayList<>();

    @Override
    public void registerEvents(BouncyBulletGame game) {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerMoveEvent.class)
                .filter(PlayerMoveEvent::hasExplicitlyChangedPosition)
                .filter(event -> !event.hasChangedOrientation())
                .handler(event -> lastMoved.put(event.getPlayer().getUniqueId(), System.currentTimeMillis()))
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(GunShootEvent.class)
                .filter(event -> event.getGunItem() == GameItems.SNIPER_RIFLE)
                .handler(event -> {
                    Player player = event.getShooter();
                    BouncyBulletGamePlayer gamePlayer = game.findPlayer(player.getUniqueId());

                    if (gamePlayer.getLoadout().playerClass() != this)
                        return;

                    BulletProperties bulletProperties = event.getBulletProperties();

                    double originalDamage = bulletProperties.maxDamage();
                    double finalDamage = originalDamage + (originalDamage * (player.getLevel() / 100D));

                    event.setBulletProperties(bulletProperties.withMaxDamage(finalDamage));
                    this.lastMoved.put(player.getUniqueId(), System.currentTimeMillis());
                })
                .build());
    }

    @Override
    public void onStartUse(BouncyBulletGamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        tasks.add(Bukkit.getScheduler().runTaskTimer(BouncyBulletsPlugin.getInstance(), () -> {
            long interval = System.currentTimeMillis() - this.lastMoved.getOrDefault(player.getUniqueId(), System.currentTimeMillis());
            float progress = interval / 5000f;

            if (progress > 1) {
                if (player.getExp() == 1) {
                    return;
                }

                progress = 1;
            }

            player.setExp(progress);

            int damage = (int) (50 * progress);
            player.setLevel(damage);
        }, 0, 1));
    }

    @Override
    public void onStopUse(BouncyBulletGamePlayer player) {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }

    @Override
    public String getName() {
        return "Sniper";
    }

    @Override
    public TreeMap<Integer, ItemStack> getItems() {
        return new TreeMap<>() {{
            put(0, PRIMARY_GUN.buildItemStack());
            put(1, SECONDARY_GUN.buildItemStack());
//            put(2, ACTIVE_ABILITY.buildItemStack());
        }};
    }

    private static class TargetAbility extends ActiveAbilityItem {

        private static final int TIME_SECONDS = 10;
        private static final float DAMAGE_INCREASE = 1;

        TargetAbility() {
            super("sniper_active_ability", Material.SPECTRAL_ARROW, AdventureUtils.withoutItalics("Target", NamedTextColor.LIGHT_PURPLE),
                    MiniMessageUtils.wordWrapLore(List.of(
                            "<gray>Upon right-click, mark a player for <green>" + TIME_SECONDS + " <gray>seconds, causing damage to that player to be increased by <red>100%<gray>."
                    ), 30));
        }

        @Override
        public void onUse(PlayerInteractEvent event) {
            Player player = event.getPlayer();

//            MapPalette.resizeImage()
            RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 100, 2);

            if (result == null)
                return;

            if (!(result.getHitEntity() instanceof Player hitPlayer))
                return;

            GlowUtils.showGlow(player, hitPlayer);
        }
    }

}
