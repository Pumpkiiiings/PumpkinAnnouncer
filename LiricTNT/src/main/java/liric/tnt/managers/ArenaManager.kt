package liric.tnt.manager

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.infernalsuite.asp.api.loaders.SlimeLoader
import com.infernalsuite.asp.api.world.properties.SlimeProperties
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap
import com.infernalsuite.asp.loaders.file.FileLoader
import liric.tnt.LiricTNTPlugin
import liric.tnt.game.Arena
import liric.tnt.game.TntRun
import liric.tnt.game.TntTag
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.concurrent.CompletableFuture

class ArenaManager(private val plugin: LiricTNTPlugin) {

    private val asp = AdvancedSlimePaperAPI.instance()
    private val fileLoader: SlimeLoader
    private val mm = MiniMessage.miniMessage()

    private val pBlue = "#B2E2F2"
    private val pGreen = "#B2F2BB"
    private val pPurple = "#E2B2F2"
    private val pRed = "#FFB7B2"

    private val arenasFile = File(plugin.dataFolder, "arenas.yml")
    private var arenasConfig = YamlConfiguration.loadConfiguration(arenasFile)

    val arenas = mutableMapOf<String, Arena>()

    var mainLobby: Location? = null
    var waitingSpawn: Location? = null

    init {
        val slimeFolder = File(plugin.dataFolder, "slime_worlds")
        if (!slimeFolder.exists()) slimeFolder.mkdirs()
        this.fileLoader = FileLoader(slimeFolder)

        loadGlobalLocations()
    }

    fun loadStoredArenas() {
        val section = arenasConfig.getConfigurationSection("arenas") ?: return
        val keys = section.getKeys(false)

        plugin.componentLogger.info(mm.deserialize("<$pBlue>[ArenaManager] Cargando <white>${keys.size}</white> arenas desde la base de datos..."))

        for (name in keys) {
            val type = arenasConfig.getString("arenas.$name.type") ?: "tag"
            setupArena(name, type).thenAccept { arena ->
                if (arena != null) {
                    plugin.componentLogger.info(mm.deserialize("<$pGreen>[ArenaManager] Arena <white>$name</white> lista."))
                }
            }
        }
    }

    private fun saveArenaToConfig(arena: Arena) {
        val path = "arenas.${arena.name}"
        arenasConfig.set("$path.type", arena.type)

        // 🔥 MAGIA ANTI-LEAKS: Guardamos las locations sin mundo para que no se guarde el nombre del mundo temporal
        val cleanSpawns = arena.spawns.map { loc ->
            Location(null, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        }
        arenasConfig.set("$path.spawns", cleanSpawns)
        arenasConfig.save(arenasFile)
    }

    fun setupArena(templateName: String, type: String): CompletableFuture<Arena?> {
        val future = CompletableFuture<Arena?>()
        val instanceName = "${templateName}_game_${System.currentTimeMillis()}"

        plugin.server.asyncScheduler.runNow(plugin) { _ ->
            try {
                if (!fileLoader.worldExists(templateName)) {
                    plugin.componentLogger.error(mm.deserialize("<$pRed>[ArenaManager] Archivo .slime '$templateName' no encontrado."))
                    future.complete(null)
                    return@runNow
                }

                val props = SlimePropertyMap().apply {
                    setValue(SlimeProperties.ALLOW_ANIMALS, false)
                    setValue(SlimeProperties.ALLOW_MONSTERS, false)
                    setValue(SlimeProperties.PVP, true)
                    setValue(SlimeProperties.WORLD_TYPE, "flat")
                }

                val template = asp.readWorld(fileLoader, templateName, true, props)
                val worldInstance = template.clone(instanceName)

                plugin.server.globalRegionScheduler.execute(plugin) {
                    try {
                        val instance = asp.loadWorld(worldInstance, false)
                        val bukkitWorld = instance.bukkitWorld ?: return@execute

                        bukkitWorld.apply {
                            isAutoSave = false
                            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
                            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
                            setGameRule(GameRule.FALL_DAMAGE, false)
                            setGameRule(GameRule.DO_MOB_SPAWNING, false)
                            setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
                            setGameRule(GameRule.DO_FIRE_TICK, false)
                        }

                        val arena: Arena = when (type.lowercase()) {
                            "tag" -> TntTag(plugin, templateName)
                            "run" -> TntRun(plugin, templateName)
                            else -> TntTag(plugin, templateName)
                        }

                        // 🔥 LA INYECCIÓN MÁGICA 🔥
                        // Leemos los spawns y a CADA UNO le forzamos a usar el nuevo 'bukkitWorld' clonado
                        val rawSpawns = arenasConfig.getList("arenas.$templateName.spawns") as? List<Location> ?: emptyList()
                        val injectedSpawns = rawSpawns.map { loc ->
                            Location(bukkitWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
                        }

                        arena.spawns.addAll(injectedSpawns)
                        arenas[templateName] = arena

                        saveArenaToConfig(arena)

                        plugin.componentLogger.info(mm.deserialize("<$pGreen>[ArenaManager] <white>$templateName</white> cargada en <$pBlue>${bukkitWorld.name}</$pBlue> <$pPurple>[${type.uppercase()}]</$pPurple>"))
                        future.complete(arena)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        future.complete(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                future.complete(null)
            }
        }
        return future
    }

    fun addSpawn(arenaName: String, location: Location) {
        val arena = arenas[arenaName]
        if (arena != null) {
            // Añadimos la location original al juego en memoria
            arena.spawns.add(location)

            // Guardamos a disco de forma segura (Sin mundo)
            val cleanSpawns = arena.spawns.map { loc ->
                Location(null, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
            }
            arenasConfig.set("arenas.$arenaName.spawns", cleanSpawns)
            arenasConfig.save(arenasFile)
        } else {
            plugin.componentLogger.warn(mm.deserialize("<$pRed>[ArenaManager] No se pudo añadir spawn: Arena $arenaName no cargada."))
        }
    }

    fun setMainLobbyLoc(loc: Location) {
        val cleanLoc = Location(null, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        this.mainLobby = loc
        arenasConfig.set("global.mainLobby", cleanLoc)
        arenasConfig.save(arenasFile)
    }

    fun setWaitingSpawnLoc(loc: Location) {
        val cleanLoc = Location(null, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        this.waitingSpawn = loc
        arenasConfig.set("global.waitingSpawn", cleanLoc)
        arenasConfig.save(arenasFile)
    }

    private fun loadGlobalLocations() {
        // En este caso, el lobby global sí suele estar en el mundo principal ("world")
        val mainWorld = plugin.server.getWorlds().firstOrNull()

        (arenasConfig.get("global.mainLobby") as? Location)?.let {
            it.world = mainWorld
            mainLobby = it
        }

        (arenasConfig.get("global.waitingSpawn") as? Location)?.let {
            it.world = mainWorld
            waitingSpawn = it
        }
    }

    fun getArenaByName(name: String): Arena? = arenas[name]

    fun getArena(player: Player): Arena? = arenas.values.find {
        it.alivePlayers.contains(player) || it.spectators.contains(player)
    }

    fun unloadArena(arenaName: String) {
        arenas.remove(arenaName)
    }
}
