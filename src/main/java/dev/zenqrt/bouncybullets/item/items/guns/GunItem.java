package dev.zenqrt.bouncybullets.item.items.guns;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.events.GunShootEvent;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.player.BouncyBulletsHUD;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public abstract class GunItem extends GameItem {

    private static final long INTERACT_EVENT_TICK_DELAY = 4;
    private static final UUID AIM_ZOOM_MODIFIER_UUID = UUID.randomUUID();
    private static final AttributeModifier RELOAD_SLOWDOWN_MODIFIER = new AttributeModifier(UUID.randomUUID(), "reload_slowdown", -0.05, AttributeModifier.Operation.ADD_NUMBER);
    private static final NamespacedKey AMMO_KEY = new NamespacedKey(BouncyBulletsPlugin.getInstance(), "ammo");
    private static final Title AIM_CROSSHAIR_TITLE = Title.title(
            Component.empty(),
            Component.text("¯", NamedTextColor.DARK_GRAY),
            Title.Times.times(Duration.ZERO, Duration.ofDays(1), Duration.ZERO));

    private final Map<UUID, Long> lastShootTicks = new HashMap<>();
    protected final GunProperties gunProperties;
    protected final BulletProperties bulletProperties;

    public GunItem(String key, Material material, Component displayName, GunProperties gunProperties, BulletProperties bulletProperties) {
        super(key, material, displayName, buildGunPropertyDescription(bulletProperties), itemMeta ->
                itemMeta.addAttributeModifier(
                        Attribute.GENERIC_ATTACK_SPEED,
                        new AttributeModifier(UUID.randomUUID(), "gun_pullout_speed", -pullOutTicksToAttackSpeed(gunProperties.pullOutTicks()), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
                )
        );

        this.gunProperties = gunProperties;
        this.bulletProperties = bulletProperties;
    }

    private static double pullOutTicksToAttackSpeed(int pullOutTicks) {
        return 4 - 20D / pullOutTicks;
    }

    public GunItem(String key, Material material, String displayName, GunProperties gunProperties, BulletProperties bulletProperties) {
        this(key, material, AdventureUtils.withoutItalics(displayName, NamedTextColor.YELLOW), gunProperties, bulletProperties);
    }

    protected abstract void shootProjectile(Player player, BulletProperties bulletProperties);
    protected abstract Sound getShootingSound();

    @Override
    public void onInteract(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        event.setCancelled(true);

        if (event.getAction().isLeftClick()) {
            displayAimZoom(player);
        } else if (event.getAction().isRightClick()) {
            if (player.hasCooldown(this.material))
                return;

            BouncyBulletGamePlayer gamePlayer = game.findPlayer(player.getUniqueId());

            shootGun(gamePlayer, player, itemStack);
        }
    }

    private void displayAimZoom(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        assert movementSpeed != null;

        if (movementSpeed.getModifier(AIM_ZOOM_MODIFIER_UUID) != null) {
            stopAiming(player);
        } else {
            startAiming(player, this.gunProperties);
        }
    }

    private void shootGun(BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack) {
        if (player.getGameMode() != GameMode.ADVENTURE)
            return;

        if (this.gunProperties.shootDelayTicks() < INTERACT_EVENT_TICK_DELAY) {
            long shootDivisions = INTERACT_EVENT_TICK_DELAY / this.gunProperties.shootDelayTicks();

            useGun(player, gamePlayer.getHud(), itemStack);

            for (int i = 1; i < shootDivisions; i++) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        useGun(player, gamePlayer.getHud(), itemStack);
                    }
                }.runTaskLater(BouncyBulletsPlugin.getInstance(), (long) i * this.gunProperties.shootDelayTicks());
            }

            return;
        }

        useGun(player, gamePlayer.getHud(), itemStack);
    }

    private void useGun(Player player, BouncyBulletsHUD hud, ItemStack itemStack) {
        long currentGameTime = player.getWorld().getGameTime();

        if (getAmmo(itemStack) <= 0)
            return;

        if (lastShootTicks.containsKey(player.getUniqueId())) {
            long lastShootTick = lastShootTicks.get(player.getUniqueId());
            long tickInterval = currentGameTime - lastShootTick;

            if (tickInterval < this.gunProperties.shootDelayTicks()) {
                return;
            }
        }

        GunShootEvent event = new GunShootEvent(this, player, this.bulletProperties);
        Bukkit.getPluginManager().callEvent(event);

        player.getWorld().playSound(getShootingSound(), player.getX(), player.getY(), player.getZ());

        shootProjectile(player, event.getBulletProperties());
        useAmmo(itemStack, hud);

        lastShootTicks.put(player.getUniqueId(), player.getWorld().getGameTime());
    }

    private void useAmmo(ItemStack itemStack, BouncyBulletsHUD hud) {
        itemStack.editMeta(meta -> {
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            int ammo = dataContainer.getOrDefault(AMMO_KEY, PersistentDataType.INTEGER, this.gunProperties.magazineSize());
            int newAmmo = ammo - 1;

            dataContainer.set(AMMO_KEY, PersistentDataType.INTEGER, newAmmo);

            hud.updateAmmo(newAmmo, this.gunProperties.magazineSize());
            hud.updateHudText();
        });
    }

    public final void reload(BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack) {
        int ammo = getAmmo(itemStack);

        if (ammo >= this.gunProperties.magazineSize())
            return;

        int timeToReload = this.gunProperties.reloadTicksPerAmmo() * (this.gunProperties.magazineSize() - ammo);

        player.setCooldown(itemStack.getType(), timeToReload);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).addModifier(RELOAD_SLOWDOWN_MODIFIER);

        new ReloadTask(player, gamePlayer.getHud(), timeToReload, itemStack, player.getInventory().getHeldItemSlot())
                .runTaskTimer(BouncyBulletsPlugin.getInstance(), 0, this.gunProperties.reloadTicksPerAmmo());
    }

    protected Sound getReloadSound() {
        return Sound.sound(org.bukkit.Sound.ITEM_ARMOR_EQUIP_CHAIN.key(), Sound.Source.PLAYER, 1, 1);
    }

    public int getAmmo(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(AMMO_KEY, PersistentDataType.INTEGER, this.gunProperties.magazineSize());
    }

    private static List<Component> buildGunPropertyDescription(BulletProperties bulletProperties) {
        return MiniMessageUtils.wordWrapLore(List.of(
                "<gray>Damage: <red>" + bulletProperties.maxDamage() + "❤",
                "<gray>Speed: <yellow>" + bulletProperties.speed() + " blocks/sec",
                "<gray>Bounces: <yellow>" + bulletProperties.numberOfBounces()

        ), 30);
    }

    public GunProperties getGunProperties() {
        return gunProperties;
    }

    public BulletProperties getBulletProperties() {
        return bulletProperties;
    }

    public static void startAiming(Player player, GunProperties gunProperties) {
        AttributeInstance movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        assert movementSpeed != null;

        AttributeModifier zoomModifier = new AttributeModifier(
                AIM_ZOOM_MODIFIER_UUID,
                "aim_zoom",
                -0.15 * gunProperties.scopeMagnifyMultiplier(),
                AttributeModifier.Operation.ADD_SCALAR
        );
        movementSpeed.addTransientModifier(zoomModifier);
        player.showTitle(AIM_CROSSHAIR_TITLE);
    }

    public static void stopAiming(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        assert movementSpeed != null;

        player.clearTitle();
        movementSpeed.removeModifier(AIM_ZOOM_MODIFIER_UUID);
    }

    public static boolean isAiming(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        assert movementSpeed != null;

        return movementSpeed.getModifier(AIM_ZOOM_MODIFIER_UUID) != null;
    }

    private class ReloadTask extends BukkitRunnable {

        private final Player player;
        private final BouncyBulletsHUD hud;
        private final int timeToReload;
        private final ItemStack itemStack;
        private final int slot;
        private int ticks;

        ReloadTask(Player player, BouncyBulletsHUD hud, int timeToReload, ItemStack itemStack, int slot) {
            this.player = player;
            this.hud = hud;
            this.timeToReload = timeToReload;
            this.itemStack = itemStack;
            this.slot = slot;
        }

        @Override
        public void run() {
            if (player.getInventory().getHeldItemSlot() != slot || ticks >= timeToReload) {
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(RELOAD_SLOWDOWN_MODIFIER);
                player.setCooldown(itemStack.getType(), 0);
                this.cancel();
                return;
            }

            int newAmmo = getAmmo(itemStack) + 1;

            itemStack.editMeta(meta -> {
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                dataContainer.set(AMMO_KEY, PersistentDataType.INTEGER, newAmmo);
            });

            this.hud.updateAmmo(newAmmo, GunItem.this.gunProperties.magazineSize());
            this.hud.updateHudText();

            player.getInventory().setItemInMainHand(itemStack);
            player.playSound(getReloadSound(), Sound.Emitter.self());

            ticks += GunItem.this.gunProperties.reloadTicksPerAmmo();
        }
    }
}
