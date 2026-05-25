package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.game.games.states.BattleGameState;
import dev.zenqrt.bouncybullets.loadout.kit.StealthPlayerClass;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.Sounds;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.List;

public final class StealthActiveAbilityItem extends ActiveAbilityItem {

    private static final int COOLDOWN_TICKS = 900;                  // 45 seconds
    private static final Sound REFILL_SOUND = Sound.sound(Sounds.BLOCK_BREWING_STAND_BREW, Sound.Source.PLAYER, 1, 1);
    private static final Sound DRINK_SOUND = Sound.sound(Sounds.ENTITY_GENERIC_DRINK, Sound.Source.PLAYER, 1, 1);

    @SuppressWarnings("UnstableApiUsage")
    public StealthActiveAbilityItem() {
        super(
                "stealth_active_ability",
                Material.POTION,
                "Invisibility Cloak",
                MiniMessageUtils.wordWrapLore(
                        List.of(
                                "<gray>Instantly turn invisible without the stealth bar.",
                                "",
                                "<dark_gray>Cooldown: <green>" + (COOLDOWN_TICKS / 20) + "s"
                        ),
                        30
                ),
                dataComponentsBuilder()
                        .addData(
                                DataComponentTypes.POTION_CONTENTS,
                                PotionContents.potionContents()
                                        .potion(PotionType.INVISIBILITY)
                                        .build()
                        )
                        .addData(
                                DataComponentTypes.TOOLTIP_DISPLAY,
                                TooltipDisplay.tooltipDisplay()
                                        .addHiddenComponents(DataComponentTypes.POTION_CONTENTS)
                                        .build()
                        )
        );
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onUse(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        if (!StealthPlayerClass.canGoInvisible(gamePlayer))
            return;

        gamePlayer.hide();
        player.playSound(DRINK_SOUND);
        itemStack.setData(DataComponentTypes.ITEM_MODEL, Material.GLASS_BOTTLE.key());
        player.setCooldown(Material.POTION, COOLDOWN_TICKS);

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {
                    if (!(game.getGameState() instanceof BattleGameState))
                        return;

                    itemStack.setData(DataComponentTypes.ITEM_MODEL, super.material.key());
                    player.playSound(REFILL_SOUND, Sound.Emitter.self());
                },
                COOLDOWN_TICKS
        );
    }
}
