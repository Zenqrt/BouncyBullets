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
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class GunItem extends GameItem {

    private static final NamespacedKey AIM_ZOOM_MODIFIER_KEY = BouncyBulletsPlugin.createKey("aim_zoom");
    private static final AttributeModifier RELOAD_SLOWDOWN_MODIFIER = new AttributeModifier(
            BouncyBulletsPlugin.createKey("reload_slowdown"),
            -0.05,
            AttributeModifier.Operation.ADD_NUMBER
    );
    private static final NamespacedKey AMMO_KEY = new NamespacedKey(BouncyBulletsPlugin.getInstance(), "ammo");
    private static final Sound AIM_SOUND = Sound.sound(Sounds.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5F, 2F);
    private static final Title AIM_CROSSHAIR_TITLE = Title.title(
            Component.empty(),
            Component.text("^", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD),
            Title.Times.times(Duration.ZERO, Duration.ofDays(1), Duration.ZERO));

    protected final Map<UUID, Long> lastShootTicks = new HashMap<>();
    protected final GunProperties gunProperties;
    protected final BulletProperties bulletProperties;

    public GunItem(String key, Material material, Component displayName, GunProperties gunProperties, BulletProperties bulletProperties) {
        super(key, material, displayName, buildGunPropertyDescription(bulletProperties), itemMeta ->
                itemMeta.addAttributeModifier(
                        Attribute.ATTACK_SPEED,
                        new AttributeModifier(
                                BouncyBulletsPlugin.createKey("gun_pullout_speed"),
                                -pullOutTicksToAttackSpeed(gunProperties.pullOutTicks()),
                                AttributeModifier.Operation.ADD_NUMBER,
                                EquipmentSlotGroup.HAND
                        )
                )
        );

        this.gunProperties = gunProperties;
        this.bulletProperties = bulletProperties;
    }

    private static double pullOutTicksToAttackSpeed(int pullOutTicks) {
        return 4 - 20D / pullOutTicks;
    }

    public GunItem(String key, Material material, String displayName, GunProperties gunProperties, BulletProperties bulletProperties) {
        this(
                key,
                material,
                AdventureUtils.withoutItalics(displayName, NamedTextColor.YELLOW)
                        .append(
                                Component.text(" (", NamedTextColor.GRAY)
                                        .append(Component.keybind("key.swapOffhand"))
                                        .append(Component.text(" to reload)"))
                        ),
                gunProperties,
                bulletProperties
        );
    }

    protected abstract void useGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack);
    protected abstract void shootProjectile(BouncyBulletGame game, Player player, BulletProperties bulletProperties);
    protected abstract Sound getShootingSound();

    @Override
    public void onHeld(BouncyBulletGame game, Player player, ItemStack itemStack, ItemStack previousItemStack) {
        final BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        int ammo = getAmmo(itemStack);
        int magSize = this.getGunProperties().magazineSize();

        gamePlayer.getHud().updateAmmo(ammo, magSize);
        gamePlayer.getHud().updateHudText();

        player.setCooldown(itemStack.getType(), this.getGunProperties().pullOutTicks());
    }

    @Override
    public void onUnheld(BouncyBulletGame game, Player player, ItemStack itemStack, ItemStack newItemStack) {
        final BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        gamePlayer.getHud().hideAmmo();
        gamePlayer.getHud().updateHudText();
    }

    @Override
    public void onInteract(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        event.setCancelled(true);

        if (event.getAction().isLeftClick()) {
            displayAimZoom(player);
        } else if (event.getAction().isRightClick()) {
            if (player.hasCooldown(this.material))
                return;

            BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

            useGun(game, gamePlayer, player, itemStack);
        }
    }

    private void displayAimZoom(Player player) {
        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

        if (movementSpeed.getModifier(AIM_ZOOM_MODIFIER_KEY) != null) {
            stopAiming(player);
        } else {
            startAiming(player, this.gunProperties);
        }
    }

    protected final void shootGun(BouncyBulletGame game, Player player, BouncyBulletsHUD hud, ItemStack itemStack) {
        GunShootEvent event = new GunShootEvent(this, player, this.bulletProperties);
        Bukkit.getPluginManager().callEvent(event);

        player.getWorld().playSound(getShootingSound(), player);

        shootProjectile(game, player, event.getBulletProperties());
        useAmmo(itemStack, hud);
    }

    protected final void useAmmo(ItemStack itemStack, BouncyBulletsHUD hud) {
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
        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

        movementSpeed.removeModifier(RELOAD_SLOWDOWN_MODIFIER);
        movementSpeed.addTransientModifier(RELOAD_SLOWDOWN_MODIFIER);

        player.setCooldown(itemStack.getType(), timeToReload);

        new ReloadTask(player, gamePlayer.getHud(), timeToReload, itemStack, player.getInventory().getHeldItemSlot())
                .runTaskTimer(BouncyBulletsPlugin.getInstance(), 0, this.gunProperties.reloadTicksPerAmmo());
    }

    protected Sound getReloadSound() {
        return Sound.sound(Sounds.ITEM_ARMOR_EQUIP_CHAIN, Sound.Source.PLAYER, 1, 1);
    }

    public int getAmmo(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(AMMO_KEY, PersistentDataType.INTEGER, this.gunProperties.magazineSize());
    }

    private static List<Component> buildGunPropertyDescription(BulletProperties bulletProperties) {
        return MiniMessageUtils.wordWrapLore(List.of(
                "<gray>Damage: <red>" + bulletProperties.maxDamage() + "❤",
                "<gray>Speed: <yellow>" + bulletProperties.speed() + " blocks/s",
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
        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

        AttributeModifier zoomModifier = new AttributeModifier(
                AIM_ZOOM_MODIFIER_KEY,
                -0.15 * gunProperties.scopeMagnifyMultiplier(),
                AttributeModifier.Operation.ADD_SCALAR
        );
        movementSpeed.addTransientModifier(zoomModifier);
        player.showTitle(AIM_CROSSHAIR_TITLE);
        player.playSound(AIM_SOUND, Sound.Emitter.self());
    }

    public static void stopAiming(Player player) {
        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

        player.clearTitle();
        movementSpeed.removeModifier(AIM_ZOOM_MODIFIER_KEY);
    }

    public static boolean isAiming(Player player) {
        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

        return movementSpeed.getModifier(AIM_ZOOM_MODIFIER_KEY) != null;
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
                AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

                movementSpeed.removeModifier(RELOAD_SLOWDOWN_MODIFIER);
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
