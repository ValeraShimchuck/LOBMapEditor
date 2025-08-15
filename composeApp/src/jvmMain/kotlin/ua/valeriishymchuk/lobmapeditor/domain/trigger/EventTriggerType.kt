package ua.valeriishymchuk.lobmapeditor.domain.trigger

enum class EventTriggerType(
    val key: String
) {

    ON_TURN_START("onTurnStart"),
    ON_TURN_END("onTurnEnd")

}