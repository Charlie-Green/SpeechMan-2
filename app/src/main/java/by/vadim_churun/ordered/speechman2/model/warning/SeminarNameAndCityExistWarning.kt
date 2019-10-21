package by.vadim_churun.ordered.speechman2.model.warning

import by.vadim_churun.ordered.speechman2.db.entities.Seminar


class SeminarNameAndCityExistWarning(
    val ID: Int?,
    val name: String,
    val city: String,
    val address: String,
    val content: String,
    val costing: Seminar.CostingStrategy,
    val isLogicallyDeleted: Boolean
): DataWarning<Seminar>()
{
    override fun produceObject(): Seminar
        = Seminar(ID, name, city, address, content, null, costing, isLogicallyDeleted)
}