package ua.valeriishymchuk.lobmapeditor.domain.trigger

enum class ConditionLogicType(
    val key: String
) {
    OR("OR"),
    AND("AND");

    companion object {
        fun findByKey(key: String): ConditionLogicType {
            return entries.firstOrNull { it.key == key }
                ?: throw NoSuchElementException("Can't find condition with key $key")
        }
    }

}