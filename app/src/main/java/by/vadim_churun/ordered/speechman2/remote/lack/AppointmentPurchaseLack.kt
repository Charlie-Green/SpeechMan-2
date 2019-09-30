package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Appointment
import by.vadim_churun.ordered.speechman2.db.objs.HistoryStatus
import by.vadim_churun.ordered.speechman2.db.objs.Money


class AppointmentPurchaseLack(
    var personID: Int,
    var seminarID: Int,
    var cost: Money,
    var historyStatus: HistoryStatus,
    var isDeleted: Boolean
): DataLack<Money, Appointment>()
{
    override fun validate(potentialData: Money): Boolean
        = true

    override fun buildObject(missedData: Money): Appointment
        = Appointment(personID, seminarID, missedData, cost, historyStatus, isDeleted)
}