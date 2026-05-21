package ua.valeriishymchuk.lobmapeditor.domain.reference.address

data class ObjectAddress(
    val triggerId: Int, val actionId: Int, val address: List<Int>
)