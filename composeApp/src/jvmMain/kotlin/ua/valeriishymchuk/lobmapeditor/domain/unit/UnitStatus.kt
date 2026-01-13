package ua.valeriishymchuk.lobmapeditor.domain.unit

enum class UnitStatus(
    val id: Int
) {
    STANDING(1),
    ROUTING(2),
    RECOVERING(3);

    companion object {
        fun fromId(id: Int): UnitStatus? {
            return entries.firstOrNull { it.id == id }
        }
    }

}