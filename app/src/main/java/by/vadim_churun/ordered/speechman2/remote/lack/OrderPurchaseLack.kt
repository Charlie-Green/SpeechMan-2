package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Order
import by.vadim_churun.ordered.speechman2.db.objs.*


class OrderPurchaseLack(
    var personID: Int,
    var productID: Int,
    var historyStatus: HistoryStatus,
    var isDeleted: Boolean
): DataLack<Money, Order>()
{
    override fun validate(potentialData: Money): Boolean
        = true

    override fun buildObject(missedData: Money): Order
        = Order(personID, productID, missedData, historyStatus, isDeleted)
}