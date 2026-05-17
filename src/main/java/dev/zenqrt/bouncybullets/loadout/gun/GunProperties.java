package dev.zenqrt.bouncybullets.loadout.gun;

public record GunProperties(
        long shootDelayTicks,
        double recoilRange,
        double recoilRangeFocused,
        int magazineSize,
        int reloadTicksPerAmmo,
        int scopeMagnifyMultiplier
) {}
