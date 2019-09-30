package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Seminar


class SeminarCityLack(var ID: Int?,
    var name: String,
    var address: String,
    var content: String,
    var costing: Seminar.CostingStrategy,
    var isDeleted: Boolean
): DataLack<String, Seminar>()
{
    override fun validate(potentialData: String): Boolean
        = potentialData.isNotEmpty()

    override fun buildObject(missedData: String): Seminar
        = Seminar(
            ID = null,
            name = name,
            city = missedData,
            content = content,
            address = address,
            imageUri = null,    // Device-specific Uri's are never persisted on a remote server.
            costing = costing,
            isLogicallyDeleted = isDeleted
        )
}