package com.tucreador.turrets.ecs;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.util.Vector;

public class Components {

    public static class Transform {
        public Location location;
        public Vector direction = new Vector(0, 0, 1);
    }

    // El enlace entre ECS y las entidades de Paper
    public static class PaperSync {
        public Slime hitbox;
        public ItemDisplay visual;
    }

    public static class Turret {
        public Player owner;
        public int ticksAlive = 0;
        public int maxDurationTicks = 20 * 15; // 15 segundos
        public double damage = 0.5; // Daño bajo
        public double range = 25.0;
        public LivingEntity currentTarget;
    }

    public static class Bullet {
        public Vector velocity;
        public double speed = 3.0; // Bloques por tick
        public int ticksAlive = 0;
        public int maxLife = 40; // 2 segundos máximo
        public Turret sourceTurret;
    }
}
