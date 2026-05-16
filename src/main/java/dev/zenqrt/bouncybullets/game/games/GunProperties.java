package dev.zenqrt.bouncybullets.game.games;

public record GunProperties(
        long shootDelayTicks,
        double recoilRange,
        double recoilRangeFocused,
        int magazineSize,
        int reloadTicksPerAmmo,
        int scopeMagnifyMultiplier
) {}
