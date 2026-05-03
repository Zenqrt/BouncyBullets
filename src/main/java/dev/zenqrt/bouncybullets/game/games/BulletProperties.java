package dev.zenqrt.bouncybullets.game.games;

public record BulletProperties(int numberOfBounces, double speed, float speedChange, double minDamage, double maxDamage, float damageChange, double effectiveDamageDist, double damageDropOffPerBlock) {

    public BulletProperties withMaxDamage(double maxDamage) {
        return new BulletProperties(numberOfBounces, speed, speedChange, minDamage, maxDamage, damageChange, effectiveDamageDist, damageDropOffPerBlock);
    }

}
