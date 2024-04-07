package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.event.GunShootEvent;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public abstract class GunItem extends GameItem {

    private static final long INTERACT_EVENT_TICK_DELAY = 4;
    private static final AttributeModifier RELOAD_SLOWDOWN_MODIFIER = new AttributeModifier(UUID.randomUUID(), "reload_slowdown", -0.05, AttributeModifier.Operation.ADD_NUMBER);
    private static final NamespacedKey AMMO_KEY = new NamespacedKey(BouncyBullets.getInstance(), "ammo");

    private final Map<UUID, Long> lastShootTicks = new HashMap<>();
    protected final Gun gun;

    public GunItem(String key, Material material, Component displayName, Gun gun) {
        super(key, material, displayName, buildGunPropertyDescription(gun));

        this.gun = gun;
    }

    public GunItem(String key, Material material, String displayName, Gun gun) {
        this(key, material, AdventureUtils.withoutItalics(displayName, NamedTextColor.YELLOW), gun);
    }

    protected abstract void shootProjectile(Player player, BulletProperties bulletProperties);
    protected abstract Sound getShootingSound();

    protected Sound getReloadSound() {
        return Sound.sound(org.bukkit.Sound.ITEM_ARMOR_EQUIP_CHAIN.key(), Sound.Source.PLAYER, 1, 1);
    }

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

                        useGun(player, itemStack);

                        for (int i = 1; i < shootDivisions; i++) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    useGun(player, itemStack);
                                }
                            }.runTaskLater(BouncyBullets.getInstance(), i * gun.getGunProperties().shootDelayTicks());
                        }

                        return;
                    }

                    useGun(player, itemStack);
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
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, gun.getGunProperties().scopeMagnifyMultiplier(), false, false, false));
                    }
                })
                .build());

        this.eventNode.registerListener(PaperEventListener.builder(PlayerItemHeldEvent.class)
                .filter(event -> event.getPlayer().getGameMode() == GameMode.ADVENTURE)
                .filter(event -> filterGameItem(event.getPlayer().getInventory().getItem(event.getNewSlot()), this))
                .handler(event -> {
                    Player player = event.getPlayer();
                    Bukkit.getScheduler().runTaskTimer(BouncyBullets.getInstance(), task -> {
                        if (!(filterGameItem(player.getInventory().getItemInMainHand(), this) && player.getGameMode() == GameMode.ADVENTURE)) {
                            player.sendActionBar(Component.empty());
                            task.cancel();
                            return;
                        }

                        player.sendActionBar(MiniMessage.miniMessage().deserialize("<gray>Ammo:</gray> <aqua>" + getAmmo(Objects.requireNonNull(player.getInventory().getItem(event.getNewSlot()))) + "<dark_gray>/</dark_gray>" + gun.getGunProperties().magazineSize() + "<gray> | </gray>"));
                    }, 0, 1);
                })
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerSwapHandItemsEvent.class)
                .filter(event -> filterGameItem(event.getOffHandItem(), this))
                .handler(event -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();
                    ItemStack itemStack = event.getOffHandItem();
                    int ammo = getAmmo(itemStack);

                    if (ammo >= gun.getGunProperties().magazineSize())
                        return;

                    int timeToReload = gun.getGunProperties().reloadTicksPerAmmo() * (gun.getGunProperties().magazineSize() - ammo);

                    player.setCooldown(itemStack.getType(), timeToReload);
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).addModifier(RELOAD_SLOWDOWN_MODIFIER);

                    new ReloadTask(player, timeToReload, itemStack, player.getInventory().getHeldItemSlot())
                            .runTaskTimer(BouncyBullets.getInstance(), 0, gun.getGunProperties().reloadTicksPerAmmo());
                })
                .build());
    }

    private void useGun(Player player, ItemStack itemStack) {
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

        GunShootEvent event = new GunShootEvent(gun, player, gun.getBulletProperties());
        Bukkit.getPluginManager().callEvent(event);

        player.getWorld().playSound(getShootingSound(), player.getX(), player.getY(), player.getZ());

        shootProjectile(player, event.getBulletProperties());
        useAmmo(itemStack);

        lastShootTicks.put(player.getUniqueId(), player.getWorld().getGameTime());
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

    private static List<Component> buildGunPropertyDescription(Gun gun) {
        return MiniMessageUtils.wordWrapLore(List.of(
                "<gray>Damage: <red>" + gun.getBulletProperties().damage() + "❤",
                "<gray>Speed: <yellow>" + gun.getBulletProperties().speed() + " b/s",
                "<gray>Bounces: <yellow>" + gun.getBulletProperties().numberOfBounces()

        ), 30);
    }

    private class ReloadTask extends BukkitRunnable {

        private final Player player;
        private final int timeToReload;
        private final ItemStack itemStack;
        private final int slot;
        private int ticks;

        ReloadTask(Player player, int timeToReload, ItemStack itemStack, int slot) {
            this.player = player;
            this.timeToReload = timeToReload;
            this.itemStack = itemStack;
            this.slot = slot;
        }

        @Override
        public void run() {
            if (player.getInventory().getHeldItemSlot() != slot || ticks >= timeToReload) {
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(RELOAD_SLOWDOWN_MODIFIER);
                this.cancel();
                return;
            }

            itemStack.editMeta(meta -> {
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                dataContainer.set(AMMO_KEY, PersistentDataType.INTEGER, getAmmo(itemStack) + 1);
            });

            player.getInventory().setItemInMainHand(itemStack);

            player.playSound(getReloadSound(), Sound.Emitter.self());

            ticks += gun.getGunProperties().reloadTicksPerAmmo();
        }
    }
}
