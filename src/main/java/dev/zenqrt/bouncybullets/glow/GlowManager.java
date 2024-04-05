package dev.zenqrt.bouncybullets.glow;

import java.util.*;

public final class GlowManager {

    private static final Map<UUID, List<Integer>> glowing = new HashMap<>();

    public static void addGlowing(UUID player, int targetEntityId) {
        List<Integer> glowingList = glowing.getOrDefault(player, new ArrayList<>());
        glowingList.add(targetEntityId);

        glowing.put(player, glowingList);
    }

    public static void removeGlowing(UUID player, int targetEntityId) {
        List<Integer> glowingList = glowing.getOrDefault(player, new ArrayList<>());
        glowingList.remove(targetEntityId);

        glowing.put(player, glowingList);
    }

    public static boolean isGlowing(UUID player, int targetEntityId) {
        List<Integer> glowingList = glowing.getOrDefault(player, new ArrayList<>());
        return glowingList.contains(targetEntityId);
    }


}
