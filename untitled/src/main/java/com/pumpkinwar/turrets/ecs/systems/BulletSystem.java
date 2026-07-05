package com.tucreador.turrets.ecs.systems;

import com.tucreador.turrets.ecs.Components.*;
import com.tucreador.turrets.ecs.ECSRegistry;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.RayTraceResult;

public class BulletSystem {
    private final ECSRegistry registry;

    public BulletSystem(ECSRegistry registry) {
        this.registry = registry;
    }

    public void update(int entityId) {
        Bullet bullet = registry.getComponent(entityId, Bullet.class);
        Transform transform = registry.getComponent(entityId, Transform.class);
        PaperSync sync = registry.getComponent(entityId, PaperSync.class);

        if (bullet == null) return;

        bullet.ticksAlive++;
        if (bullet.ticksAlive > bullet.maxLife) {
            destroyBullet(entityId, sync);
            return;
        }

        Location currentLoc = transform.location;
        Location nextLoc = currentLoc.clone().add(bullet.velocity);

        // RayTrace para impactos precisos
        RayTraceResult hit = currentLoc.getWorld().rayTrace(
                currentLoc,
                bullet.velocity.normalize(),
                bullet.speed,
                FluidCollisionMode.NEVER,
                true,
                0.2, // Tamaño de hitbox del láser
                entity -> entity instanceof org.bukkit.entity.LivingEntity && entity != bullet.sourceTurret.owner
        );

        if (hit != null && hit.getHitEntity() instanceof org.bukkit.entity.LivingEntity target) {
            // Evitar golpear torretas (Slimes invisibles)
            if (!(target instanceof org.bukkit.entity.Slime && target.isInvisible())) {

                // === ANTI CURACIÓN Y DAÑO 1200 RPM ===
                // Quitamos los ticks de invulnerabilidad (i-frames) para que el daño aplique cada tick
                target.setNoDamageTicks(0);
                target.damage(bullet.sourceTurret.damage, bullet.sourceTurret.owner);

                target.getWorld().spawnParticle(Particle.CRIT, hit.getHitPosition().toLocation(target.getWorld()), 3);
                destroyBullet(entityId, sync);
                return;
            }
        } else if (hit != null && hit.getHitBlock() != null) {
            // Chocó con pared
            currentLoc.getWorld().spawnParticle(Particle.BLOCK, hit.getHitPosition().toLocation(currentLoc.getWorld()), 5, hit.getHitBlock().getBlockData());
            destroyBullet(entityId, sync);
            return;
        }

        // Mover bala si no hubo impacto
        transform.location = nextLoc;
        sync.visual.teleport(nextLoc); // Paper/Cliente interpolan este teleport gracias a teleportDuration(1)
    }

    private void destroyBullet(int entityId, PaperSync sync) {
        if (sync != null && sync.visual != null) {
            sync.visual.remove();
        }
        registry.destroyEntity(entityId);
    }
}
