package liric.boss.bosses.impl

import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes
import liric.boss.LiricBossPlugin
import liric.boss.bosses.LiricBoss
import liric.boss.utils.Chat
import liric.boss.utils.PacketUtils
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TitanAbisal(plugin: LiricBossPlugin) : LiricBoss(plugin, "titan_abisal", Chat.format(plugin.config.getString("bosses.titan_abisal.name")!!)) {

    private var tickCounter = 0

    override fun spawn(location: Location) {
        val world = location.world ?: return
        val warden = world.spawnEntity(location, EntityType.WARDEN) as Warden
        entity = warden

        warden.let {
            it.customName(displayName)
            it.isCustomNameVisible = true
            it.removeWhenFarAway = false
            it.getAttribute(Attribute.SCALE)?.baseValue = 1.8
            it.getAttribute(Attribute.MAX_HEALTH)?.baseValue = 2000.0 // Tanque masivo
            it.health = 2000.0
            it.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue = 40.0
            it.getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = 1.0
        }

        bossBar = BossBar.bossBar(displayName, 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.NOTCHED_10)
        Bukkit.broadcast(Chat.PREFIX.append(Chat.format(plugin.config.getString("bosses.titan_abisal.messages.spawn")!!)))
    }

    override fun onTick() {
        if (isDeadManWalking) return
        val ent = entity as? Warden ?: return
        if (ent.isDead) return

        tickCounter++
        updateBossBar()

        // Mantenerlo enojado con el jugador más cercano para que no escarbe
        if (tickCounter % 40 == 0) {
            val target = ent.location.getNearbyPlayers(30.0).firstOrNull()
            if (target != null) ent.target = target
            PacketUtils.spawnParticle(ent.location.add(0.0, 1.0, 0.0), ParticleTypes.SCULK_SOUL, 10, 1f, 1f, 1f, 0.05f)
        }

        if (tickCounter % 180 == 0) castSkill(ent)
    }

    private fun castSkill(ent: Warden) {
        val players = ent.location.getNearbyPlayers(20.0).toList()
        if (players.isEmpty()) return
        val target = players.random()
        val skill = Random.nextInt(8)

        when (skill) {
            0 -> {
                // 1. LANZAMIENTO DE ROCA (BLOQUE DISPLAY GIGANTE)
                sendMessageToTargets(players, "<#5500aa>¡Malakor arranca la tierra misma!</#5500aa>")
                ent.world.playSound(ent.location, Sound.ENTITY_WARDEN_ROAR, 2f, 1f)

                // Crea un bloque de Obsidiana de 3x3 visualmente
                val roca = ent.world.spawn(ent.location.add(0.0, 4.0, 0.0), BlockDisplay::class.java) {
                    it.block = Material.CRYING_OBSIDIAN.createBlockData()
                    it.transformation = Transformation(Vector3f(), Quaternionf(), Vector3f(3.0f), Quaternionf())
                }

                val dir = target.location.toVector().subtract(ent.location.toVector()).normalize().multiply(1.2)

                object : org.bukkit.scheduler.BukkitRunnable() {
                    var life = 0
                    override fun run() {
                        if (life > 40 || !roca.isValid) {
                            PacketUtils.spawnParticle(roca.location, ParticleTypes.EXPLOSION_EMITTER, 1)
                            roca.world.playSound(roca.location, Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.5f)
                            roca.remove()
                            cancel()
                            return
                        }
                        roca.teleport(roca.location.add(dir))
                        roca.getNearbyPlayers(3.5).forEach { p ->
                            p.damage(30.0, ent) // Daño masivo
                            p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 60, 1))
                        }
                        life++
                    }
                }.runTaskTimer(plugin, 0L, 1L)
            }
            1 -> {
                // 2. COLMILLOS EN CRUZ (EVOKER FANGS)
                sendMessageToTargets(players, "<dark_purple>¡Las sombras te muerden!</dark_purple>")
                val loc = ent.location
                for (i in 1..10) {
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        loc.world.spawnEntity(loc.clone().add(i.toDouble(), 0.0, i.toDouble()), EntityType.EVOKER_FANGS)
                        loc.world.spawnEntity(loc.clone().add(-i.toDouble(), 0.0, i.toDouble()), EntityType.EVOKER_FANGS)
                        loc.world.spawnEntity(loc.clone().add(i.toDouble(), 0.0, -i.toDouble()), EntityType.EVOKER_FANGS)
                        loc.world.spawnEntity(loc.clone().add(-i.toDouble(), 0.0, -i.toDouble()), EntityType.EVOKER_FANGS)
                    }, i * 2L)
                }
            }
            2 -> {
                // 3. ONDA SÍSMICA SÓNICA
                sendMessageToTargets(players, "<#5500aa>¡Cúbranse los oídos!</#5500aa>")
                ent.world.playSound(ent.location, Sound.ENTITY_WARDEN_SONIC_BOOM, 3f, 0.5f)
                for (i in 0..359 step 20) {
                    val rad = Math.toRadians(i.toDouble())
                    val dir = Vector(cos(rad), 0.0, sin(rad)).normalize()
                    for (dist in 1..15 step 2) {
                        val pLoc = ent.location.clone().add(dir.clone().multiply(dist)).add(0.0, 1.0, 0.0)
                        PacketUtils.spawnParticle(pLoc, ParticleTypes.SONIC_BOOM, 1)
                    }
                }
                players.forEach { p ->
                    p.damage(20.0, ent)
                    p.velocity = Vector(0.0, 1.2, 0.0)
                }
            }
            3 -> {
                // 4. AGUJERO NEGRO ABISAL
                sendMessageToTargets(players, "<dark_gray>La gravedad se invierte...</dark_gray>")
                object : org.bukkit.scheduler.BukkitRunnable() {
                    var life = 0
                    override fun run() {
                        if (life > 60 || ent.isDead) { cancel(); return }
                        PacketUtils.spawnParticle(ent.location.add(0.0, 2.0, 0.0), ParticleTypes.REVERSE_PORTAL, 50, 4f, 4f, 4f, 0.1f)
                        players.forEach { p ->
                            val pull = ent.location.toVector().subtract(p.location.toVector()).normalize().multiply(0.4)
                            p.velocity = pull
                        }
                        life++
                    }
                }.runTaskTimer(plugin, 0L, 1L)
            }
            4 -> {
                // 5. CORRUPCIÓN DEL SUELO (Lava Visual y Daño)
                sendMessageToTargets(players, "<#ff0000>¡El suelo es magma sculk!</#00000>")
                val center = ent.location
                object : org.bukkit.scheduler.BukkitRunnable() {
                    var life = 0
                    override fun run() {
                        if (life > 80) { cancel(); return }
                        PacketUtils.spawnParticle(center, ParticleTypes.LAVA, 100, 8f, 0.1f, 8f, 0f)
                        players.forEach { p ->
                            if (p.location.distance(center) < 8.0 && p.location.y <= center.y + 1) {
                                p.damage(5.0)
                                p.fireTicks = 40
                            }
                        }
                        life += 5
                    }
                }.runTaskTimer(plugin, 0L, 5L)
            }
            5 -> {
                // 6. GEYSER SCULK
                val pLoc = target.location.clone()
                PacketUtils.spawnParticle(pLoc, ParticleTypes.SCULK_CHARGE_POP, 100, 1f, 0f, 1f, 0f)
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    PacketUtils.spawnParticle(pLoc, ParticleTypes.SONIC_BOOM, 5, 0f, 3f, 0f, 0f)
                    pLoc.getNearbyPlayers(2.0).forEach { p ->
                        p.velocity = Vector(0.0, 2.5, 0.0)
                        p.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 100, 1))
                    }
                }, 20L)
            }
            6 -> {
                // 7. INVOCACIÓN DE SOMBRAS
                sendMessageToTargets(players, "<dark_purple>¡Sirvientes de la noche, ataquen!</dark_purple>")
                ent.world.playSound(ent.location, Sound.ENTITY_VEX_AMBIENT, 2f, 0.5f)
                for (i in 1..4) {
                    val vex = ent.world.spawnEntity(ent.location.add(0.0, 2.0, 0.0), EntityType.VEX) as Vex
                    vex.customName(Chat.format("<dark_gray>Sombra de Malakor</dark_gray>"))
                    vex.isCustomNameVisible = true
                    vex.equipment?.setItemInMainHand(ItemStack(Material.NETHERITE_SWORD))
                }
            }
            7 -> {
                // 8. CEGUERA TOTAL
                ent.world.playSound(ent.location, Sound.AMBIENT_CAVE, 3f, 0.1f)
                players.forEach { p ->
                    p.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 200, 0))
                    p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 100, 2))
                    p.sendMessage(Chat.format("<dark_purple>Tus ojos te traicionan...</dark_purple>"))
                }
            }
        }
    }

    private fun sendMessageToTargets(players: List<org.bukkit.entity.Player>, msg: String?) {
        if (msg == null) return
        players.forEach { it.sendMessage(Chat.PREFIX.append(Chat.format(msg))) }
    }

    override fun onDeath() {
        if (isDeadManWalking) return
        isDeadManWalking = true
        val ent = entity ?: return

        Bukkit.broadcast(Chat.PREFIX.append(Chat.format(plugin.config.getString("bosses.titan_abisal.messages.death")!!)))
        PacketUtils.spawnParticle(ent.location, ParticleTypes.SONIC_BOOM, 20, 2f, 2f, 2f, 0.5f)
        ent.world.playSound(ent.location, Sound.ENTITY_WARDEN_DEATH, 2f, 0.5f)

        plugin.config.getStringList("bosses.titan_abisal.rewards").forEach { cmd ->
            ent.world.getNearbyPlayers(ent.location, 50.0).forEach { p ->
                Bukkit.getScheduler().runTask(plugin, Runnable { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.name)) })
            }
        }
        bossBar.viewers().toList().forEach { bossBar.removeViewer(it as Player) }
    }
}
