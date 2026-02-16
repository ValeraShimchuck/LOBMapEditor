package ua.valeriishymchuk.lobmapeditor.domain.trigger

enum class ConditionLogicType(
    val key: String,
    val displayName: String
) {
    OR("OR", "Any Condition(OR)"),
    AND("AND", "All Conditions(AND)");

    companion object {
        fun findByKey(key: String): ConditionLogicType {
            return entries.firstOrNull { it.key == key }
                ?: throw NoSuchElementException("Can't find condition with key $key")
        }
    }

}