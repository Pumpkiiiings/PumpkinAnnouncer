package liric.taquitos.cocina.listener

import liric.taquitos.service.CookingService

class CookingListener(private val cookingService: CookingService) : Listener {

    @EventHandler
    fun onComalInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val player = event.player

        // Usaremos el Ahumador (Smoker) como nuestra estación de cocina
        if (block.type == Material.SMOKER) {
            event.isCancelled = true // Evitar que se abra el inventario original

            if (event.action == Action.RIGHT_CLICK_BLOCK) {
                val itemInHand = player.inventory.itemInMainHand

                // Lógica simple por ahora: si tiene carne, empieza a cocinar
                if (itemInHand.type == Material.BEEF || itemInHand.type == Material.PORK_CHOP) {
                    cookingService.startCooking(player, block)
                } else if (itemInHand.type == Material.AIR) {
                    // Si le da clic con la mano vacía, intenta recoger lo que hay
                    cookingService.stopCooking(block)
                    player.sendMessage("¡Recogiste el ingrediente!")
                }
            }
        }
    }
}