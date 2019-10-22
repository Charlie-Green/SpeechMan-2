package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Appointment
import by.vadim_churun.ordered.speechman2.db.objs.*


class AppointmentMoneyLack(
    var personID: Int,
    var seminarID: Int,
    var historyStatus: HistoryStatus,
    var isDeleted: Boolean
): DataLack<AppointmentMoneyLack.MissingData, Appointment>()
{
    class MissingData(
        val purchase: Money,
        val cost: Money )

    override fun validate(potentialData: MissingData): Boolean
        = true

    override fun buildObject(missedData: MissingData): Appointment
        = Appointment(personID,
            seminarID,
            missedData.purchase,
            missedData.cost,
            historyStatus,
            isDeleted
        )
}