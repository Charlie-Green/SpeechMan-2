package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.SemCost
import by.vadim_churun.ordered.speechman2.db.objs.Money
import java.util.Calendar


class SemCostMoneyLack(
    var seminarID: Int,
    var minParticipants: Int,
    var minDate: Calendar
): DataLack<Money, SemCost>()
{
    override fun validate(potentialData: Money): Boolean
        = true

    override fun buildObject(missedData: Money): SemCost
        = SemCost(null, seminarID, minParticipants, minDate, missedData)
}