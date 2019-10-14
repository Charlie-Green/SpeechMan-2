package by.vadim_churun.ordered.speechman2.model.lack_info

import java.util.Calendar
import by.vadim_churun.ordered.speechman2.db.entities.Seminar

sealed class DataLackInfo
{
    class AppointmentInfo(
        val personName: String,
        val seminarName: String
    ): DataLackInfo()
    class OrderInfo(
        val personName: String,
        val productName: String
    ): DataLackInfo()
    class SemCostInfo(
        val seminarName: String,
        val costing: Seminar.CostingStrategy,
        val minParticipants: Int,
        val minDate: Calendar
    ): DataLackInfo()
}