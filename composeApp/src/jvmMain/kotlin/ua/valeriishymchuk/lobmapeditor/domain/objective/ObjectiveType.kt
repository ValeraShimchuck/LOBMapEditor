package ua.valeriishymchuk.lobmapeditor.domain.objective

enum class ObjectiveType(
    val id: Int,
    val defaultVictoryPoints: Int,
) {
    SMALL(1, 15),
    BIG(2, 75);

    companion object {
        fun getTypeById(id: Int): ObjectiveType {
            return ObjectiveType.entries.first { it.id == id }
        }
    }

}