package by.vadim_churun.ordered.speechman2.db.entities

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.objs.*


/** Association between [Person] and [Seminar]. **/
@Entity(
    tableName = "Appointments",
    primaryKeys = ["person", "seminar"] )
class Appointment(
    // @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="person", index = true) val personID: Int,

    // @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="seminar", index = true) val seminarID: Int,

    @ColumnInfo(name="cost") val cost: Money,

    @ColumnInfo(name="history") val historyStatus: HistoryStatus,

    @ColumnInfo(name="isDeleted", index = true) val isLogicallyDeleted: Boolean
)