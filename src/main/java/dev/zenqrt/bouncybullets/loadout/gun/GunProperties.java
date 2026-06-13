package dev.zenqrt.bouncybullets.loadout.gun;

public record GunProperties(
        int pullOutTicks,
        int shootDelayTicks,
        double spreadRange,
        double spreadRangeFocused,
        double recoilPitch,
        double recoilYaw,
        double recoilPitchFocused,
        double recoilYawFocused,
        int magazineSize,
        int reloadTicks,
        int scopeMagnifyMultiplier
) {}
