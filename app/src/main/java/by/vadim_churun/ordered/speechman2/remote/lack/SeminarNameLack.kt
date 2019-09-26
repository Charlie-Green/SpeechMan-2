package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Seminar


class SeminarNameLack(val city: String,
    val address: String,
    val content: String,
    val costing: Seminar.CostingStrategy,
    val isDeleted: Boolean
): DataLack<String, Seminar>()
{
    override fun validate(potentialData: String): Boolean
        = potentialData.isNotEmpty()

    override fun buildObject(missedData: String): Seminar
        = Seminar(
            ID = null,
            name = missedData,
            city = city,
            content = content,
            address = address,
            imageUri = null,    // Device-specific Uri's are never persisted on a remote server.
            costing = costing,
            isLogicallyDeleted = isDeleted
        )
}