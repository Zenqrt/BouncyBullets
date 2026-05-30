package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.EventNode;
import dev.zenqrt.bouncybullets.event.PaperEventListener;
import dev.zenqrt.bouncybullets.event.events.GunShootEvent;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class HeavyPlayerClass implements EventPlayerClass {

    private static final int FIRING_DAMAGE_DEBUFF_TICKS = 4;
    private static final AttributeModifier HEALTH_BUFF_MODIFIER = new AttributeModifier(
            BouncyBulletsPlugin.createKey("heavy_health_buff"),
            20,
            AttributeModifier.Operation.ADD_NUMBER
    );
    private static final AttributeModifier SLOW_MODIFIER = new AttributeModifier(
            BouncyBulletsPlugin.createKey("heavy_speed"),
            -0.04,
            AttributeModifier.Operation.ADD_NUMBER
    );


    private final Object2LongMap<UUID> lastShotTicks = new Object2LongOpenHashMap<>();

    @Override
    public String getName() {
        return "Heavy";
    }

    @Override
    public List<GunItem> getGuns() {
        return List.of(
                GameItems.MINIGUN
        );
    }

    @Override
    public List<ActiveAbilityItem> getActiveAbilities() {
        return List.of(
                GameItems.HEAVY_ACTIVE_ABILITY
        );
    }

    @Override
    public Map<EquipmentSlot, ItemStack> getArmorEquipment() {
        return Map.of(
                EquipmentSlot.HEAD, new ItemStack(Material.TINTED_GLASS),
                EquipmentSlot.CHEST, new ItemStack(Material.NETHERITE_CHESTPLATE),
                EquipmentSlot.LEGS, new ItemStack(Material.IRON_LEGGINGS),
                EquipmentSlot.FEET, new ItemStack(Material.NETHERITE_BOOTS)
        );
    }

    @Override
    public EventNode<Event> registerEvents(BouncyBulletGame game) {
        EventNode<Event> eventNode = EventNode.create();

        eventNode.registerListener(PaperEventListener.builder(EntityDamageEvent.class)
                .filter(event -> event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)
                .filter(event -> event.getEntity() instanceof Player player && isPlayerClass(game, player, this))
                .filter(event -> shouldTakeExtraDamage(event.getEntity(), game))
                .handler(event -> event.setDamage(event.getDamage() * 2))
                .build());

        eventNode.registerListener(PaperEventListener.builder(GunShootEvent.class)
                .filter(event -> isPlayerClass(game, event.getShooter(), this))
                .handler(event -> {
                    long currentTick = event.getShooter().getWorld().getGameTime();

                    this.lastShotTicks.put(event.getShooter().getUniqueId(), currentTick);
                })
                .build()
        );

        return eventNode;
    }

    private boolean shouldTakeExtraDamage(Entity entity, BouncyBulletGame game) {
        long currentTick = entity.getWorld().getGameTime();

        return currentTick - this.lastShotTicks.getLong(entity.getUniqueId()) >= FIRING_DAMAGE_DEBUFF_TICKS
                || game.findPlayerOrThrow(entity.getUniqueId()).isReloading();
    }

    @Override
    public void onStartUse(BouncyBulletGamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        AttributeInstance maxHealth = PlayerUtils.requireNonNullAttribute(player, Attribute.MAX_HEALTH);
        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

        maxHealth.addTransientModifier(HEALTH_BUFF_MODIFIER);
        player.setHealth(maxHealth.getValue());

        movementSpeed.addTransientModifier(SLOW_MODIFIER);
    }

    @Override
    public void onStopUse(BouncyBulletGamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        AttributeInstance maxHealth = PlayerUtils.requireNonNullAttribute(player, Attribute.MAX_HEALTH);
        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

        maxHealth.removeModifier(HEALTH_BUFF_MODIFIER);
        movementSpeed.removeModifier(SLOW_MODIFIER);
    }
}
