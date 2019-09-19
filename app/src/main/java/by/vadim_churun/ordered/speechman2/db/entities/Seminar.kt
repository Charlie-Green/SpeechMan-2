package by.vadim_churun.ordered.speechman2.db.entities

import android.content.Context
import android.net.Uri
import androidx.room.*
import by.vadim_churun.ordered.speechman2.R


@Entity(tableName = "Seminars")
class Seminar(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id", index=true) val ID: Int?,

    @ColumnInfo(name="name") val name: String,

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


    companion object
    {
        private val dateDependingCostings = listOf(
            CostingStrategy.DATE,
            CostingStrategy.DATE_PARTICIPANTS,
            CostingStrategy.PARTICIPANTS_DATE )
        fun doesCostDependOnDate(strategy: CostingStrategy): Boolean
            = dateDependingCostings.contains(strategy)

        private val participsDependingCostings = listOf(
            CostingStrategy.PARTICIPANTS,
            CostingStrategy.PARTICIPANTS_DATE,
            CostingStrategy.DATE_PARTICIPANTS )
        fun doesCostDependOnParticipants(strategy: CostingStrategy): Boolean
            = participsDependingCostings.contains(strategy)

        private val mapCostingToResid = hashMapOf(
            CostingStrategy.FIXED to R.string.costing_fixed,
            CostingStrategy.PARTICIPANTS to R.string.costing_participants,
            CostingStrategy.DATE to R.string.costing_date,
            CostingStrategy.PARTICIPANTS_DATE to R.string.costing_participants_date,
            CostingStrategy.DATE_PARTICIPANTS to R.string.costing_date_participants
        )
        fun costingToString(context: Context, costing: CostingStrategy): String
            = context.getString( mapCostingToResid[costing]!! )
    }
}