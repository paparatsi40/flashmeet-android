package com.carlitoswy.flashmeet.domain.model

enum class AdOption(
    val displayName: String,
    val description: String,
    val priceUsd: Double
) {
    NONE("Gratis", "Evento básico sin promoción", 0.0),
    HIGHLIGHTED("Destacado", "Aparece visualmente resaltado", 1.99),
    PROMOTED("Promocionado", "Prioridad en mapa y lista", 3.99),
    BANNER("Banner", "Visible en sección destacada de inicio", 6.99);

    fun displayPrice(): String = if (priceUsd == 0.0) "Gratis" else "$${String.format("%.2f", priceUsd)}"
}
fun AdOption.toFirestoreString(): String = this.name

fun String?.toAdOption(): AdOption =
    AdOption.entries.find { it.name.equals(this, ignoreCase = true) } ?: AdOption.NONE
