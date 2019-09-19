package by.vadim_churun.ordered.speechman2.db.entities

import androidx.room.*
import java.util.Calendar


@Entity(
    tableName = "SemDays",
    foreignKeys = [
        ForeignKey(entity = Seminar::class, parentColumns = ["id"], childColumns = ["seminar"])
    ]
)
class SemDay(
    @PrimaryKey
    @ColumnInfo(name="id", index=true) val ID: Int?,

    @ColumnInfo(name="seminar", index=true) val seminarID: Int,

    /** Date and time when this SemDay starts. **/
    @ColumnInfo(name="start") val start: Calendar,

    /** How long the SemDay lasts for, in minutes. **/
    @ColumnInfo(name="duration") val duration: Short
)