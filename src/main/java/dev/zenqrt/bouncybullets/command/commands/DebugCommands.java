package dev.zenqrt.bouncybullets.command.commands;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public final class DebugCommands {

    private static final Path RESOURCE_PACK_PATH = Path.of("C:\\Users\\craft\\Documents\\Development\\GitHub\\BouncyBullets\\server\\plugins\\BouncyBullets\\packs\\resource_pack.zip");

    @Command("pack send")
    public void onPackSend(
            Player player
    ) throws Exception {
        player.sendResourcePacks(
                ResourcePackInfo.resourcePackInfo()
                        .uri(URI.create("http://localhost:8000/resource_pack.zip"))
                        .hash(sha1(RESOURCE_PACK_PATH))
                        .id(UUID.randomUUID())
                        .build()
        );
    }

    @Command("pack remove")
    public void onPackRemove(
            Player player
    ) {
        player.removeResourcePacks();
    }

    @Command("pack reload")
    public void onPackReload(
            Player player
    ) throws Exception {
        player.removeResourcePacks();
        player.sendResourcePacks(
                ResourcePackInfo.resourcePackInfo()
                        .uri(URI.create("http://localhost:8000/resource_pack.zip"))
                        .hash(sha1(RESOURCE_PACK_PATH))
                        .id(UUID.randomUUID())
                        .build()
        );
    }

    private static String sha1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

}
