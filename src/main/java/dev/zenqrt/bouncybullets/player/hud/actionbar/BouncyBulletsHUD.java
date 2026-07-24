package dev.zenqrt.bouncybullets.player.hud.actionbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class BouncyBulletsHUD extends ActionBarHUD {

    private static final String AMMO_HUD_ID = "ammo";

    public void updateAmmo(int ammo, int maxAmmo) {
        if (super.hasDisplay(AMMO_HUD_ID))
            super.updateDisplay(AMMO_HUD_ID, ammoText(ammo, maxAmmo));
        else
            super.addDisplay(0, AMMO_HUD_ID, ammoText(ammo, maxAmmo));
    }

    public void hideAmmo() {
        super.removeDisplay(AMMO_HUD_ID);
    }

    private static Component ammoText(int ammo, int maxAmmo) {
        return MiniMessage.miniMessage().deserialize("<gray>Ammo:</gray> <aqua>" + ammo + "<dark_gray>/</dark_gray>" + maxAmmo);
    }

}
