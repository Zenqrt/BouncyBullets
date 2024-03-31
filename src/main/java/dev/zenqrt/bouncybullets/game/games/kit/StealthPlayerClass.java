package dev.zenqrt.bouncybullets.game.games.kit;

import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.items.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

final class StealthPlayerClass extends EventPlayerClass {

    private static final Gun PRIMARY_GUN = Gun.SMG;
    private static final Gun SECONDARY_GUN = Gun.SILENCED_PISTOL;
    private static final InvisibilityAbility ACTIVE_ABILITY = new InvisibilityAbility();

    @Override
    public String getName() {
        return "Stealth";
    }

    @Override
    public void giveItems(PlayerInventory inventory) {
        inventory.setItem(0, PRIMARY_GUN.buildItemStack());
        inventory.setItem(1, SECONDARY_GUN.buildItemStack());
        inventory.setItem(2, ACTIVE_ABILITY.buildItemStack());
    }

    @Override
    public void registerEvents(BouncyBulletPlayer player) {
        GameItem.registerGameItemEvents(List.of(ACTIVE_ABILITY));
        this.eventNode.registerListener(PaperEventListener.builder(PlayerDeathEvent.class)
                .filter(event -> {
                    EntityDamageEvent lastDamageEvent = event.getPlayer().getLastDamageCause();

                    if (lastDamageEvent != null) {
                        return lastDamageEvent.getDamageSource().getCausingEntity().equals(player.player());
                    }

                    return false;
                })
                .handler(event -> player.player().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 2, false, true, true))).build());
    }

    private static class InvisibilityAbility extends ActiveAbilityItem {

        InvisibilityAbility() {
            super("stealth_active_ability", Material.POTION, AdventureUtils.withoutItalics("Invisibility Cloak", NamedTextColor.LIGHT_PURPLE), List.of(), 1200);
        }

        @Override
        public void onUse(PlayerInteractEvent event) {
            Player player = event.getPlayer();

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 5, 1, false, true, true));
            player.playSound(Sound.sound(org.bukkit.Sound.ENTITY_GENERIC_DRINK, Sound.Source.PLAYER, 1, 1));

            event.getItem().setType(Material.GLASS_BOTTLE);
        }
    }
}
