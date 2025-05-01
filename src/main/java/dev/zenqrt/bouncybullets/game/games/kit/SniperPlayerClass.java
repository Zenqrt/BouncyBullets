package dev.zenqrt.bouncybullets.game.games.kit;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.event.GunShootEvent;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.item.items.ActiveAbilityItem;
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
final class SniperPlayerClass extends EventPlayerClass {

    private static final TargetAbility ACTIVE_ABILITY = new TargetAbility();

    private final Map<UUID, Long> lastMoved = new HashMap<>();
    private final List<BukkitTask> tasks = new ArrayList<>();

    @Override
    public void registerEvents(BouncyBulletPlayer player) {
        ACTIVE_ABILITY.registerEvents();

        this.eventNode.registerListener(PaperEventListener.builder(PlayerMoveEvent.class)
                .filter(PlayerMoveEvent::hasExplicitlyChangedPosition)
                .filter(event -> !event.hasChangedOrientation())
                .handler(event -> lastMoved.put(event.getPlayer().getUniqueId(), System.currentTimeMillis()))
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(GunShootEvent.class)
                .filter(event -> event.getShooter().equals(player.player()))
                .filter(event -> event.getGun() == Gun.SNIPER_RIFLE)
                .handler(event -> {
                    Player playerEntity = player.player();

                    double originalDamage = Gun.SNIPER_RIFLE.getBulletProperties().damage();
                    double finalDamage = originalDamage + (originalDamage * (playerEntity.getLevel() / 100D));

                    event.setBulletProperties(event.getBulletProperties().withDamage(finalDamage));
                    lastMoved.put(playerEntity.getUniqueId(), System.currentTimeMillis());
                })
                .build());
    }

    @Override
    public void onStartUse(BouncyBulletPlayer player) {
        Player playerEntity = player.player();

        tasks.add(Bukkit.getScheduler().runTaskTimer(BouncyBullets.getInstance(), () -> {
            long interval = System.currentTimeMillis() - lastMoved.getOrDefault(playerEntity.getUniqueId(), System.currentTimeMillis());
            float progress = interval / 5000f;

            if (progress > 1) {
                if (playerEntity.getExp() == 1) {
                    return;
                }

                progress = 1;
            }

            playerEntity.setExp(progress);

            int damage = (int) (50 * progress);
            playerEntity.setLevel(damage);
        }, 0, 1));
    }

    @Override
    public void onStopUse(BouncyBulletPlayer player) {
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
            put(0, Gun.SNIPER_RIFLE.buildItemStack());
            put(1, Gun.PISTOL.buildItemStack());
            put(2, ACTIVE_ABILITY.buildItemStack());
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

            RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 100, 2);

            if (result == null)
                return;

            if (!(result.getHitEntity() instanceof Player hitPlayer))
                return;

            GlowUtils.showGlow(player, hitPlayer);
        }
    }

}
