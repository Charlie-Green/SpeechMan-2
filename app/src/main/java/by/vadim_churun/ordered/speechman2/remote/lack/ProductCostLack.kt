package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Product
import by.vadim_churun.ordered.speechman2.db.objs.Money


class ProductCostLack(var ID: Int?,
    var name: String,
    var countBoxes: Int,
    var countCase: Int,
    var isDeleted: Boolean
): DataLack<Money, Product>()
{
    override fun validate(potentialData: Money): Boolean
        = true

    override fun buildObject(missedData: Money): Product
        = Product(ID, name, missedData, countBoxes, countCase, isDeleted)
}