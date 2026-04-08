package liric.taco.hologui.model

import org.bukkit.Location
import org.bukkit.util.Vector

enum class ElementType { TEXT, BUTTON }

data class HoloGuiConfig(
    val id: String,
    var cameraLocation: Location?,
    val elements: Map<String, HoloGuiElement>
)

data class HoloGuiElement(
    val id: String,
    val type: ElementType,
    val offset: Vector,
    val width: Float = 1.0f,
    val height: Float = 1.0f,
    val text: List<String>,
    val clickActions: List<String> = emptyList()
)

// Representa un menú abierto por un jugador
data class ActiveHoloGui(
    val menuId: String,
    val entities: MutableList<org.bukkit.entity.Entity>,
    val interactionMap: MutableMap<java.util.UUID, List<String>> // UUID de la Entity -> Acciones
)
