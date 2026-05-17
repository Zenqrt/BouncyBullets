package dev.zenqrt.bouncybullets.loadout.gun;

public record GunProperties(
        int pullOutTicks,
        int shootDelayTicks,
        double recoilRange,
        double recoilRangeFocused,
        int magazineSize,
        int reloadTicksPerAmmo,
        int scopeMagnifyMultiplier
) {}
