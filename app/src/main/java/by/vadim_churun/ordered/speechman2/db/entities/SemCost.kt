package by.vadim_churun.ordered.speechman2.db.entities

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.objs.Money
import java.util.Calendar


/** Represents a cost which may be recommended for a seminar is certain conditions are  **/
@Entity(
    tableName = "SemCosts",
    foreignKeys = [
        ForeignKey(entity = Seminar::class, parentColumns = ["id"], childColumns = ["seminar"])
    ]
)
class SemCost(
    @PrimaryKey
    @ColumnInfo(name="id", index=true) val ID: Int?,

    @ColumnInfo(name="seminar", index=true) val seminarID: Int,

    @ColumnInfo(name="particips") val minParticipants: Int,

    @ColumnInfo(name="date") val minDate: Calendar,

    @ColumnInfo(name="cost") val cost: Money
)