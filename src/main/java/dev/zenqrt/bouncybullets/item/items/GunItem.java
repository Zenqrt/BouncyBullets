package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class GunItem extends GameItem {

    private static final long INTERACT_EVENT_TICK_DELAY = 4;
    private static final Sound HIT_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP.key(), Sound.Source.PLAYER, 1, 2);
    private static final NamespacedKey AMMO_KEY = new NamespacedKey(BouncyBullets.getInstance(), "ammo");

    private final Gun gun;
    private final Map<UUID, Long> lastShootTicks = new HashMap<>();

    public GunItem(String key, Material material, Component displayName, Gun gun) {
        super(key, material, displayName, buildGunPropertyDescription(gun));

        this.gun = gun;
    }

    public GunItem(String key, Material material, String displayName, Gun gun) {
        this(key, material, AdventureUtils.withoutItalics(displayName, NamedTextColor.YELLOW), gun);
    }

    protected abstract Sound getShootingSound();
    protected abstract ParticleBuilder getBulletParticleBuilder();

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> filterGameItem(event.getItem(), this))
                .filter(event -> event.getAction().isRightClick())
                .handler(event -> {
                    Player player = event.getPlayer();
                    ItemStack itemStack = event.getItem();

                    if (player.getGameMode() != GameMode.ADVENTURE)
                        return;

                    if (gun.getGunProperties().shootDelayTicks() < INTERACT_EVENT_TICK_DELAY) {
                        long shootDivisions = INTERACT_EVENT_TICK_DELAY / gun.getGunProperties().shootDelayTicks();

                        fireGun(player, itemStack);

                        for (int i = 1; i < shootDivisions; i++) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    fireGun(player, itemStack);
                                }
                            }.runTaskLater(BouncyBullets.getInstance(), i * gun.getGunProperties().shootDelayTicks());
                        }

                        return;
                    }

                    fireGun(player, itemStack);
                })
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> filterGameItem(event.getItem(), this))
                .filter(event -> event.getAction().isLeftClick())
                .handler(event -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();

                    if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                        player.removePotionEffect(PotionEffectType.SLOW);
                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2, false, false, false));
                    }
                })
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerSwapHandItemsEvent.class)
                .filter(event -> filterGameItem(event.getOffHandItem(), this))
                .handler(event -> {
                    event.setCancelled(true);
                    System.out.println("Pressed!");

                    Player player = event.getPlayer();
                    ItemStack itemStack = event.getOffHandItem();
                    int slot = event.getPlayer().getInventory().getHeldItemSlot();
                    int ammo = getAmmo(itemStack);

                    if (ammo >= gun.getGunProperties().magazineSize())
                        return;

                    int timeToReload = gun.getGunProperties().reloadTicksPerAmmo() * (gun.getGunProperties().magazineSize() - ammo);
                    player.setCooldown(itemStack.getType(), timeToReload);

                    AttributeInstance speedAttribute = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED));
                    AttributeModifier reloadSlowdown = new AttributeModifier(UUID.randomUUID(), "reload_slowdown", -0.025, AttributeModifier.Operation.ADD_NUMBER);

                    speedAttribute.addModifier(reloadSlowdown);

                    new BukkitRunnable() {

                        private int ticks;

                        @Override
                        public void run() {
                            if (player.getInventory().getHeldItemSlot() != slot || ticks >= timeToReload) {
                                speedAttribute.removeModifier(reloadSlowdown);
                                this.cancel();
                                return;
                            }

                            itemStack.editMeta(meta -> {
                                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                                dataContainer.set(AMMO_KEY, PersistentDataType.INTEGER, getAmmo(itemStack) + 1);
                            });

                            player.getInventory().setItemInMainHand(itemStack);

                            Sound reloadingSound = Sound.sound(org.bukkit.Sound.ITEM_ARMOR_EQUIP_CHAIN.key(), Sound.Source.PLAYER, 1, 1);
                            player.playSound(reloadingSound, Sound.Emitter.self());

                            ticks += gun.getGunProperties().reloadTicksPerAmmo();

                        }
                    }.runTaskTimer(BouncyBullets.getInstance(), 0, gun.getGunProperties().reloadTicksPerAmmo());
                })
                .build());
    }

    private void fireGun(Player player, ItemStack itemStack) {
        long currentGameTime = player.getWorld().getGameTime();

        if (getAmmo(itemStack) <= 0)
            return;

        if (lastShootTicks.containsKey(player.getUniqueId())) {
            long lastShootTick = lastShootTicks.get(player.getUniqueId());
            long tickInterval = currentGameTime - lastShootTick;

            if (tickInterval < gun.getGunProperties().shootDelayTicks()) {
                return;
            }
        }

        shootBullet(player);
        useAmmo(itemStack);
    }

    private int getAmmo(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(AMMO_KEY, PersistentDataType.INTEGER, gun.getGunProperties().magazineSize());
    }

    private void useAmmo(ItemStack itemStack) {
        itemStack.editMeta(meta -> {
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            int ammo = dataContainer.getOrDefault(AMMO_KEY, PersistentDataType.INTEGER, gun.getGunProperties().magazineSize());

            dataContainer.set(AMMO_KEY, PersistentDataType.INTEGER, ammo - 1);
        });
    }

    private void shootBullet(Player player) {
        lastShootTicks.put(player.getUniqueId(), player.getWorld().getGameTime());

        player.getWorld().playSound(getShootingSound(), player.getX(), player.getY(), player.getZ());
        new ShootBulletTask(player, player.getEyeLocation(), player.hasPotionEffect(PotionEffectType.SLOW)).runTaskTimer(BouncyBullets.getInstance(), 0, 1);
    }

    private static List<Component> buildGunPropertyDescription(Gun gun) {
        return MiniMessageUtils.wordWrapLore(List.of(
                "<gray>Damage: <red>" + gun.getBulletProperties().damage() + "❤",
                "<gray>Speed: <yellow>" + gun.getBulletProperties().speed() + " b/s",
                "<gray>Bounces: <yellow>" + gun.getBulletProperties().numberOfBounces()

        ), 30);
    }

    private class ShootBulletTask extends BukkitRunnable {

        private final Player shooter;
        private Location bounceLocation;
        private Location lastBulletLocation;
        private Vector currentDirection;
        private int bounces = 0;
        private int tickSinceBounce = 0;
        private int currentTick = 0;

        private ShootBulletTask(Player shooter, Location startLocation, boolean focused) {
            this.shooter = shooter;

            this.bounceLocation = startLocation;
            this.lastBulletLocation = startLocation;

            double recoilRange = focused ? gun.getGunProperties().recoilRangeFocused() : gun.getGunProperties().recoilRange();
            this.currentDirection = startLocation.getDirection().normalize()
                    .add(new Vector(randomRecoil(recoilRange), randomRecoil(recoilRange), randomRecoil(recoilRange)))
                    .normalize();
        }

        private static double randomRecoil(double range) {
            return ThreadLocalRandom.current().nextDouble(-range, range);
        }

        @Override
        public void run() {
            if (currentTick++ >= 200) {
                this.cancel();
                return;
            }

            BulletProperties bulletProperties = gun.getBulletProperties();

            double segmentLength = (bulletProperties.speed() + (bulletProperties.speed() * (bulletProperties.speedChange() * bounces))) / 20;
            double distance = segmentLength * tickSinceBounce++;
            Vector increment = currentDirection.clone().multiply(distance);
            Location location = bounceLocation.clone().add(increment);

            spawnBulletParticle(location);

            RayTraceResult result = location.getWorld().rayTrace(location, currentDirection, segmentLength, FluidCollisionMode.NEVER, true, 0.1, entity -> entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE && player != shooter);

            if (result != null) {
                Location hitLocation = result.getHitPosition().toLocation(location.getWorld());

                spawnCenterParticle(lastBulletLocation, hitLocation);

                if (result.getHitEntity() instanceof Player hitPlayer) {
                    double damage = bulletProperties.damage() + (bulletProperties.damage() * (bulletProperties.damageChange() * bounces));

                    hitPlayer.damage(damage, DamageSource.builder(DamageType.MOB_PROJECTILE).withCausingEntity(shooter).build());
                    hitPlayer.setNoDamageTicks(0);

                    shooter.playSound(HIT_SOUND, Sound.Emitter.self());
                    this.cancel();

                    return;
                } else {
                    if (++bounces > bulletProperties.numberOfBounces()) {
                        this.cancel();
                        return;
                    }

                    this.bounceLocation = hitLocation;
                    this.lastBulletLocation = hitLocation;
                    this.tickSinceBounce = 0;

                    Block hitBlock = Objects.requireNonNull(result.getHitBlock());

                    Particle.BLOCK_CRACK.builder()
                            .location(hitLocation)
                            .allPlayers()
                            .force(true)
                            .count(10)
                            .extra(0.5)
                            .data(hitBlock.getBlockData())
                            .spawn();

                    Sound hitSound = Sound.sound(hitBlock.getBlockSoundGroup().getBreakSound().key(), Sound.Source.BLOCK, 1, 1);
                    hitLocation.getWorld().playSound(hitSound, hitLocation.getX(), hitLocation.getY(), hitLocation.getZ());

                    BlockFace blockFace = result.getHitBlockFace();

                    if (blockFace == null) {
                        return;
                    }

                    switch (blockFace) {
                        case UP, DOWN -> this.currentDirection = currentDirection.setY(-currentDirection.getY());
                        case EAST, WEST -> this.currentDirection = currentDirection.setX(-currentDirection.getX());
                        case NORTH, SOUTH -> this.currentDirection = currentDirection.setZ(-currentDirection.getZ());
                    }
                }

                return;
            }

            spawnCenterParticle(lastBulletLocation, location);
            this.lastBulletLocation = location;
        }
    }

    private void spawnCenterParticle(Location firstLocation, Location secondLocation) {
        double distance = firstLocation.distance(secondLocation);
        Location centerLocation = firstLocation.clone().add(secondLocation).multiply(0.5);

        spawnBulletParticle(centerLocation);

        if (distance < 1.5) {
            return;
        }

        spawnCenterParticle(firstLocation, centerLocation);
        spawnCenterParticle(centerLocation, secondLocation);
    }

    private void spawnBulletParticle(Location location) {
        getBulletParticleBuilder()
                .location(location)
                .allPlayers()
                .force(true)
                .spawn();
    }
}
