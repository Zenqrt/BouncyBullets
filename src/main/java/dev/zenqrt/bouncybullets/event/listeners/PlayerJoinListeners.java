package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.game.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class PlayerJoinListeners implements Listener {

    private final GameManager gameManager;

    public PlayerJoinListeners(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

//        PlayerUtils.injectPacketListener(
//                event.getPlayer(),
//                "test",
//                new ChannelDuplexHandler() {
//                    @Override
//                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                        if (msg instanceof ServerboundUseItemPacket packet)
//                            System.out.println("Found server action packet with: " + packet.getHand());
//
//                        super.channelRead(ctx, msg);
//                    }
//                }
//        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(null);

        UUID uuid = event.getPlayer().getUniqueId();

        if (!this.gameManager.isInGame(uuid))
            return;

        this.gameManager.tryLeaveGame(uuid);
    }

}
