package dev.zenqrt.bouncybullets.sidebar;

import dev.zenqrt.bouncybullets.utils.NMSConverter;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PacketSidebar {

    private final List<ServerPlayer> viewers = new ArrayList<>();
    private final List<SidebarLine> lines = new ArrayList<>();
    private final Objective objective;
    private final Scoreboard scoreboard;

    public PacketSidebar(Component title) {
        this.scoreboard = new Scoreboard();
        this.objective = new Objective(
                this.scoreboard,
                UUID.randomUUID().toString(),
                ObjectiveCriteria.DUMMY,
                NMSConverter.component(title),
                ObjectiveCriteria.RenderType.INTEGER,
                false,
                BlankFormat.INSTANCE
        );
    }

    public void addLine(String id, Component text) {
        this.lines.add(new SidebarLine(id, NMSConverter.component(text)));
    }

    public void addEmptyLine() {
        this.lines.add(new SidebarLine(UUID.randomUUID().toString(), net.minecraft.network.chat.Component.empty()));
    }

    public void addViewer(ServerPlayer player) {
        ClientboundSetObjectivePacket setObjectivePacket = new ClientboundSetObjectivePacket(this.objective, 0);
        ClientboundSetDisplayObjectivePacket displayObjectivePacket = new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, this.objective);

        player.connection.send(setObjectivePacket);

        for (int i = 0; i < lines.size(); i++) {
            SidebarLine line = lines.get(i);

            PlayerTeam team = new PlayerTeam(this.scoreboard, line.id());
            team.setPlayerPrefix(line.text());

            String entryId = UUID.randomUUID().toString();

            ClientboundSetPlayerTeamPacket addTeamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
            ClientboundSetPlayerTeamPacket addEntryPacket = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, entryId, ClientboundSetPlayerTeamPacket.Action.ADD);
            ClientboundSetScorePacket scorePacket = new ClientboundSetScorePacket(
                    entryId,
                    this.objective.getName(),
                    i,
                    Optional.of(net.minecraft.network.chat.Component.empty()),
                    Optional.empty()
            );

            player.connection.send(addTeamPacket);
            player.connection.send(addEntryPacket);
            player.connection.send(scorePacket);
        }

        player.connection.send(displayObjectivePacket);
        this.viewers.add(player);
    }

    public void removeViewer(ServerPlayer player) {
        ClientboundSetObjectivePacket removeObjectivePacket = createRemoveObjectivePacket();

        player.connection.send(removeObjectivePacket);
        this.viewers.remove(player);
    }

    public void removeAllViewers() {
        ClientboundSetObjectivePacket removeObjectivePacket = createRemoveObjectivePacket();

        this.viewers.forEach(player -> player.connection.send(removeObjectivePacket));
        this.viewers.clear();
    }

    public void updateLine(String id, Component text) {
        PlayerTeam team = new PlayerTeam(this.scoreboard, id);
        team.setPlayerPrefix(NMSConverter.component(text));

        ClientboundSetPlayerTeamPacket modifyTeamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
        this.viewers.forEach(player -> player.connection.send(modifyTeamPacket));
    }

    private ClientboundSetObjectivePacket createRemoveObjectivePacket() {
        return new ClientboundSetObjectivePacket(this.objective, 1);
    }

}
