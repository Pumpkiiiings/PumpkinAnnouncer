package liric.casino.scratch

import org.bukkit.Material

data class ScratchPrize(
    val material: Material,
    val displayName: String,
    val payout: Double,
    val weight: Int
)

object PrizeRegistry {
    // A mayor 'weight' (peso), más probabilidades hay de que salga ese ítem.
    val prizes = listOf(
        ScratchPrize(Material.DIAMOND_BLOCK, "<#00FFFF><bold>¡3X DIAMANTES!</bold>", 50000.0, 1),   // Súper raro
        ScratchPrize(Material.EMERALD_BLOCK, "<#00FF7F><bold>¡3X ESMERALDAS!</bold>", 10000.0, 4),  // Raro
        ScratchPrize(Material.GOLD_BLOCK, "<#FFD700><bold>¡3X ORO!</bold>", 2500.0, 15),          // Poco común
        ScratchPrize(Material.IRON_BLOCK, "<#E0E0E0><bold>¡3X HIERRO!</bold>", 1000.0, 30),         // Común (Recuperas el dinero)
        ScratchPrize(Material.COAL_BLOCK, "<dark_gray><bold>CARBÓN</bold>", 0.0, 50),             // Basura (Pierdes)
        ScratchPrize(Material.APPLE, "<red><bold>MANZANA</bold>", 0.0, 50)                        // Basura (Pierdes)
    )

    fun getRandomPrize(): ScratchPrize {
        val totalWeight = prizes.sumOf { it.weight }
        var random = (0 until totalWeight).random()
        for (prize in prizes) {
            if (random < prize.weight) return prize
            random -= prize.weight
        }
        return prizes.last()
    }
}
