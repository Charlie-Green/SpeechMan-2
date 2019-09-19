package by.vadim_churun.ordered.speechman2.db.entities

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.objs.*


/** Association between [Person] and [Seminar]. **/
@Entity(
    tableName = "Appointments",
    primaryKeys = ["person", "seminar"] )
class Appointment(
    @ColumnInfo(name="person", index = true) val personID: Int,

    @ColumnInfo(name="seminar", index = true) val seminarID: Int,

    @ColumnInfo(name="paid") val purchase: Money,

    @ColumnInfo(name="cost") val cost: Money,

    @ColumnInfo(name="history") val historyStatus: HistoryStatus,

    @ColumnInfo(name="isDeleted", index = true) val isLogicallyDeleted: Boolean
)