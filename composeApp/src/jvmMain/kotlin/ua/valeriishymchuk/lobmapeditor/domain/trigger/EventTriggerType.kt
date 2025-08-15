package ua.valeriishymchuk.lobmapeditor.domain.trigger

enum class EventTriggerType(
    val key: String
) {

    ON_TURN_START("onTurnStart"),
    ON_TURN_END("onTurnEnd");

    companion object {
        fun findByKey(key: String): EventTriggerType {
            return entries.firstOrNull { it.key == key }
                ?: throw NoSuchElementException("Can't find event with key $key")
        }
    }

}