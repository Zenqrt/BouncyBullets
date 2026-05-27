package dev.zenqrt.bouncybullets.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import dev.zenqrt.bouncybullets.utils.GuiUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class GameSelectGui extends ChestGui {

    private OutlinePane gamesListPane;
    private final PlayerSessionManager sessionManager;
    private final GameManager gameManager;

    public GameSelectGui(GameManager gameManager, PlayerSessionManager sessionManager) {
        super(4, "Available Games");

        this.gameManager = gameManager;
        this.sessionManager = sessionManager;
        this.gamesListPane = createGamesListPane(Set.of());

        this.setOnGlobalClick(event -> event.setCancelled(true));
        this.addPane(Slot.fromXY(0, 0), GuiUtils.createBackgroundPane(getRows()));
        this.addPane(Slot.fromXY(1, 1), this.gamesListPane);
    }

    private OutlinePane createGamesListPane(Set<BouncyBulletGame> games) {
        OutlinePane pane = new OutlinePane(7, 2);

        for (BouncyBulletGame game : games) {

            GuiItem guiItem = new GuiItem(
                    createGameItemStack(game),
                    event -> {
                        if (!(event.getWhoClicked() instanceof Player player))
                            return;

                        this.sessionManager.joinGame(player, game);
                    }
            );

            pane.addItem(guiItem);
        }

        return pane;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static ItemStack createGameItemStack(BouncyBulletGame game) {
        ItemStack itemStack = new ItemStack(Material.LIME_TERRACOTTA);

        itemStack.setData(
                DataComponentTypes.ITEM_NAME,
                Component.text("Game " + game.getId(), NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
        );
        itemStack.setData(
                DataComponentTypes.LORE,
                ItemLore.lore()
                        .addLine(Component.text("Map: ", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                                .append(Component.text(game.getGameMap().gameMap().displayName(), NamedTextColor.WHITE)))
                        .addLine(Component.text("Players: ", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                                .append(Component.text(game.getPlayers().size() + "/" + game.getGameSettings().maxPlayers(), NamedTextColor.WHITE)))
                        .build()
        );

        return itemStack;
    }

    public void updateGamesList() {
        this.getPanes().remove(this.gamesListPane);

        Set<BouncyBulletGame> availableGames = this.gameManager.getAvailableGames();

        this.gamesListPane = createGamesListPane(availableGames);
        this.addPane(Slot.fromXY(1, 1), this.gamesListPane);

        this.update();
    }

}
