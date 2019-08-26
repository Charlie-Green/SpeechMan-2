package by.vadim_churun.ordered.speechman2.db.entities

import android.net.Uri
import androidx.room.*


@Entity(tableName = "Seminars")
class Seminar(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id", index=true) val ID: Int,

    @ColumnInfo(name="city") val city: String,

    @ColumnInfo(name="address") val address: String,

    @ColumnInfo(name="content") val content: String,

    @ColumnInfo(name="image") val imageUri: Uri?,

    @ColumnInfo(name="costing") val costing: Seminar.CostingStrategy,

    @ColumnInfo(name="isDeleted") val isLogicallyDeleted: Boolean )
{
    enum class CostingStrategy
    {
        /** Cost is fixed. **/                                       FIXED,
        /** Cost depends on the number of participants. **/          PARTICIPANTS,
        /** Cost depends on the appointment date. **/                DATE,
        /** Appointment date first, then number of participants. **/ DATE_PARTICIPANTS,
        /** Number of participants first, then appointment date. **/ PARTICIPANTS_DATE
    }
}