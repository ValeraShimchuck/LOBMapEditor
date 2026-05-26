package ua.valeriishymchuk.lobmapeditor.domain.reference.address

data class ObjectAddress(
    val triggerId: Int, val actionId: Int, val address: List<Int>
) {

    companion object {
        fun fromRaw(rawAddress: List<Int>): ObjectAddress {
            return ObjectAddress(
                rawAddress[0],
                rawAddress[1],
                rawAddress.drop(2)
            )
        }
    }

}