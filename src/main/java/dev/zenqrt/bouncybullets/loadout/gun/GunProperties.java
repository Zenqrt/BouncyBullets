package dev.zenqrt.bouncybullets.loadout.gun;

public record GunProperties(
        int pullOutTicks,
        int shootDelayTicks,
        double spreadRange,
        double spreadRangeFocused,
        int magazineSize,
        int reloadTicks,
        int scopeMagnifyMultiplier
) {}
