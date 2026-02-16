package ua.valeriishymchuk.lobmapeditor.domain.trigger

enum class EventTriggerType(
    val key: String,
    val displayName: String
) {

    ON_TURN_START("onTurnStart", "On Turn Start"),
    ON_TURN_END("onTurnEnd", "On Turn End");

    companion object {
        fun findByKey(key: String): EventTriggerType {
            return entries.firstOrNull { it.key == key }
                ?: throw NoSuchElementException("Can't find event with key $key")
        }
    }

}