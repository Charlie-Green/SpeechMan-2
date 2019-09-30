package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Appointment
import by.vadim_churun.ordered.speechman2.db.objs.*


class AppointmentMoneyLack(
    var personID: Int,
    var seminarID: Int,
    var historyStatus: HistoryStatus,
    var isDeleted: Boolean
): DataLack<AppointmentMoneyLack.MissedData, Appointment>()
{
    class MissedData(
        val purchase: Money,
        val cost: Money )

    override fun validate(potentialData: MissedData): Boolean
        = true

    override fun buildObject(missedData: MissedData): Appointment
        = Appointment(personID,
            seminarID,
            missedData.purchase,
            missedData.cost,
            historyStatus,
            isDeleted
        )
}