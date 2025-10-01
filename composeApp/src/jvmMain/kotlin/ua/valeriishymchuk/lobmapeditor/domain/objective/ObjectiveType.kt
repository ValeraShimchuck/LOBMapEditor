package ua.valeriishymchuk.lobmapeditor.domain.objective

enum class ObjectiveType(
    val id: Int
) {
    SMALL(1),
    BIG(2);

    companion object {
        fun getTypeById(id: Int): ObjectiveType {
            return ObjectiveType.entries.first { it.id == id }
        }
    }

}