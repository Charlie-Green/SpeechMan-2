package by.vadim_churun.ordered.speechman2.model.warning

import by.vadim_churun.ordered.speechman2.db.entities.Product
import by.vadim_churun.ordered.speechman2.db.objs.Money


class ProductNameExistsWarning(val ID: Int?,
    val name: String,
    val cost: Money,
    val countBoxes: Int,
    val countCase: Int,
    val isLogicallyDeleted: Boolean
): DataWarning<Product>()
{
    override fun createObject(): Product
        = Product(ID, name, cost, countBoxes, countCase, isLogicallyDeleted)
}