package com.tucreador.turrets.ecs.systems;

import com.tucreador.turrets.ecs.Components.*;
import com.tucreador.turrets.ecs.ECSRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class TurretSystem {
    private final ECSRegistry registry;

    public TurretSystem(ECSRegistry registry) {
        this.registry = registry;
    }

    public void update(int entityId) {
        Turret turret = registry.getComponent(entityId, Turret.class);
        Transform transform = registry.getComponent(entityId, Transform.class);
        PaperSync sync = registry.getComponent(entityId, PaperSync.class);

        if (turret == null) return;

        turret.ticksAlive++;
        if (turret.ticksAlive >= turret.maxDurationTicks) {
            destroyTurret(entityId, sync);
            return;
        }

        // 1. Lógica de Targeting (Buscamos objetivo cada 10 ticks para optimizar)
        if (turret.ticksAlive % 10 == 0 || !isValidTarget(turret.currentTarget, transform.location, turret.range)) {
            turret.currentTarget = findNearestTarget(transform.location, turret.range, turret.owner);
        }

        // 2. Apuntar y Disparar
        if (turret.currentTarget != null) {
            Location eyeLoc = turret.currentTarget.getEyeLocation();
            Location gunLoc = transform.location.clone().add(0, 1.5, 0); // Altura del cañón

            // Calcular dirección
            Vector direction = eyeLoc.toVector().subtract(gunLoc.toVector()).normalize();
            transform.direction = direction;

            // Rotar visualmente la torreta (Pitch y Yaw)
            gunLoc.setDirection(direction);
            sync.visual.setRotation(gunLoc.getYaw(), gunLoc.getPitch());

            // Disparar (1 bala por tick = 1200 RPM)
            fireBullet(gunLoc, direction, turret);

            // Efecto de sonido/partícula en el cañón
            gunLoc.getWorld().playSound(gunLoc, Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 2.0f);
            gunLoc.getWorld().spawnParticle(Particle.SMOKE, gunLoc, 1, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private void fireBullet(Location loc, Vector direction, Turret source) {
        int bulletId = registry.createEntity();

        // Visual de la bala
        ItemDisplay bulletVisual = loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(Material.GLOWSTONE_DUST));
            d.setTeleportDuration(1); // Mágico: Interpola el movimiento fluido a 60fps
            d.setBillboard(ItemDisplay.Billboard.CENTER); // Mira siempre a la cámara
            Location spawnLoc = loc.clone();
            spawnLoc.setDirection(direction);
            d.teleport(spawnLoc);
        });

        Bullet bullet = new Bullet();
        bullet.velocity = direction.clone().multiply(bullet.speed);
        bullet.sourceTurret = source;

        Transform bTransform = new Transform();
        bTransform.location = loc.clone();

        PaperSync bSync = new PaperSync();
        bSync.visual = bulletVisual;

        registry.addComponent(bulletId, bullet);
        registry.addComponent(bulletId, bTransform);
        registry.addComponent(bulletId, bSync);
    }

    private boolean isValidTarget(LivingEntity target, Location loc, double range) {
        return target != null && !target.isDead() && target.isValid() &&
                target.getWorld().equals(loc.getWorld()) &&
                target.getLocation().distanceSquared(loc) <= (range * range);
    }

    private LivingEntity findNearestTarget(Location loc, double range, LivingEntity owner) {
        LivingEntity nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity e : loc.getWorld().getNearbyEntities(loc, range, range, range)) {
            if (e instanceof LivingEntity target && e != owner && e.isValid() && !e.isDead()) {
                // Evitamos apuntar a la hitbox de los slimes de las torretas
                if (target instanceof org.bukkit.entity.Slime slime && slime.isInvisible()) continue;

                double dist = target.getLocation().distanceSquared(loc);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = target;
                }
            }
        }
        return nearest;
    }

    public void destroyTurret(int entityId, PaperSync sync) {
        if (sync != null) {
            if (sync.hitbox != null) sync.hitbox.remove();
            if (sync.visual != null) sync.visual.remove();
        }
        registry.destroyEntity(entityId);
    }
}
