package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Appointment
import by.vadim_churun.ordered.speechman2.db.objs.*


class AppointmentCostLack(
    var personID: Int,
    var seminarID: Int,
    var purchase: Money,
    var historyStatus: HistoryStatus,
    var isDeleted: Boolean
): DataLack<Money, Appointment>()
{
    override fun validate(potentialData: Money): Boolean
        = true

    override fun buildObject(missedData: Money): Appointment
        = Appointment(personID, seminarID, purchase, missedData, historyStatus, isDeleted)
}