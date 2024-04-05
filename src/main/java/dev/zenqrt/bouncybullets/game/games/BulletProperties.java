package dev.zenqrt.bouncybullets.game.games;

public record BulletProperties(int numberOfBounces, double speed, float speedChange, double damage, float damageChange) {

    public BulletProperties withDamage(double damage) {
        return new BulletProperties(numberOfBounces, speed, speedChange, damage, damageChange);
    }

}
