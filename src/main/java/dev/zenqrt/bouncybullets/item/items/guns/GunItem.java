package dev.zenqrt.bouncybullets.item.items.guns;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.events.GunShootEvent;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.player.BouncyBulletsHUD;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import dev.zenqrt.bouncybullets.utils.Sounds;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.SwingAnimation;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class GunItem extends GameItem {

    private static final AttributeModifier RELOAD_SLOWDOWN_MODIFIER = new AttributeModifier(
            BouncyBulletsPlugin.createKey("reload_slowdown"),
            -0.05,
            AttributeModifier.Operation.ADD_NUMBER
    );
    private static final NamespacedKey AMMO_KEY = new NamespacedKey(BouncyBulletsPlugin.getInstance(), "ammo");

    protected final Map<UUID, Long> lastShootTicks = new HashMap<>();
    protected final GunProperties gunProperties;
    protected final BulletProperties bulletProperties;

    @SuppressWarnings("UnstableApiUsage")
    public GunItem(String key, Component displayName, GunProperties gunProperties, BulletProperties bulletProperties, DataComponentsBuilder dataComponentsBuilder) {
        super(
                key,
                Material.BOW,
                displayName,
                buildGunPropertyDescription(bulletProperties),
                dataComponentsBuilder
                        .addData(
                                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                ItemAttributeModifiers.itemAttributes()
                                        .addModifier(
                                                Attribute.ATTACK_SPEED,
                                                new AttributeModifier(
                                                        BouncyBulletsPlugin.createKey("gun_pullout_speed"),
                                                        -pullOutTicksToAttackSpeed(gunProperties.pullOutTicks()),
                                                        AttributeModifier.Operation.ADD_NUMBER,
                                                        EquipmentSlotGroup.HAND
                                                )
                                        )
                                        .build()
                        )
                        .addData(
                                DataComponentTypes.TOOLTIP_DISPLAY,
                                TooltipDisplay.tooltipDisplay()
                                        .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS)
                                        .build()
                        )
                        .addData(
                                DataComponentTypes.ITEM_MODEL,
                                new NamespacedKey("bouncybullets", key)
                        )
        );

        this.gunProperties = gunProperties;
        this.bulletProperties = bulletProperties;
    }

    private static double pullOutTicksToAttackSpeed(int pullOutTicks) {
        return 4 - 20D / pullOutTicks;
    }

    public GunItem(String key, String displayName, GunProperties gunProperties, BulletProperties bulletProperties, DataComponentsBuilder dataComponentsBuilder) {
        this(
                key,
                Component.text(displayName, NamedTextColor.YELLOW)
                        .append(
                                Component.text(" (", NamedTextColor.GRAY)
                                        .append(Component.keybind("key.swapOffhand"))
                                        .append(Component.text(" to reload)"))
                        ),
                gunProperties,
                bulletProperties,
                dataComponentsBuilder
        );
    }

    public GunItem(String key, String displayName, GunProperties gunProperties, BulletProperties bulletProperties) {
        this(key, displayName, gunProperties, bulletProperties, dataComponentsBuilder());
    }

    protected abstract void useGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack);
    protected abstract void shootProjectile(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, BulletProperties bulletProperties);
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

        if (gamePlayer.isAiming())
            gamePlayer.stopAiming(game, itemStack);
    }

    @Override
    public void onInteract(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        event.setCancelled(true);

        BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        if (event.getAction().isLeftClick()) {
            if (gamePlayer.isAiming())
                gamePlayer.stopAiming(game, itemStack);
            else
                gamePlayer.startAiming(game, this, itemStack);

        } else if (event.getAction().isRightClick()) {
            if (player.hasCooldown(this.material))
                return;

            useGun(game, gamePlayer, player, itemStack);
        }
    }

    protected final void shootGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, BouncyBulletsHUD hud, ItemStack itemStack) {
        Player player = gamePlayer.getPlayer();

        GunShootEvent event = new GunShootEvent(this, player, this.bulletProperties);
        Bukkit.getPluginManager().callEvent(event);

        player.getWorld().playSound(getShootingSound(), player);

        shootProjectile(game, gamePlayer, event.getBulletProperties());
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

        gamePlayer.setReloading(true);

        movementSpeed.removeModifier(RELOAD_SLOWDOWN_MODIFIER);
        movementSpeed.addTransientModifier(RELOAD_SLOWDOWN_MODIFIER);

        player.setCooldown(itemStack.getType(), timeToReload);

        new ReloadTask(this, gamePlayer, timeToReload, itemStack, player.getInventory().getHeldItemSlot())
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ItemStack buildItemStack() {
        ItemStack itemStack = super.buildItemStack();
        itemStack.setData(
                DataComponentTypes.SWING_ANIMATION,
                SwingAnimation.swingAnimation()
                        .type(SwingAnimation.Animation.NONE)
                        .duration(1)
        );

        return itemStack;
    }

    private static class ReloadTask extends BukkitRunnable {

        private final GunItem gunItem;
        private final BouncyBulletGamePlayer gamePlayer;
        private final int timeToReload;
        private final ItemStack itemStack;
        private final int slot;
        private int ticks;

        ReloadTask(GunItem gunItem, BouncyBulletGamePlayer gamePlayer, int timeToReload, ItemStack itemStack, int slot) {
            this.gunItem = gunItem;
            this.gamePlayer = gamePlayer;
            this.timeToReload = timeToReload;
            this.itemStack = itemStack;
            this.slot = slot;
        }

        @Override
        public void run() {
            Player player = this.gamePlayer.getPlayer();

            if (player.getInventory().getHeldItemSlot() != this.slot || this.ticks >= this.timeToReload) {
                AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

                movementSpeed.removeModifier(RELOAD_SLOWDOWN_MODIFIER);
                player.setCooldown(this.itemStack.getType(), 0);

                this.gamePlayer.setReloading(false);
                this.cancel();

                return;
            }

            int newAmmo = this.gunItem.getAmmo(this.itemStack) + 1;

            itemStack.editMeta(meta -> {
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                dataContainer.set(AMMO_KEY, PersistentDataType.INTEGER, newAmmo);
            });

            this.gamePlayer.getHud().updateAmmo(newAmmo, this.gunItem.getGunProperties().magazineSize());
            this.gamePlayer.getHud().updateHudText();

            player.getInventory().setItemInMainHand(this.itemStack);
            player.playSound(this.gunItem.getReloadSound(), Sound.Emitter.self());

            ticks += this.gunItem.getGunProperties().reloadTicksPerAmmo();
        }
    }
}
