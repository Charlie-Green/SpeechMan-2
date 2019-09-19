package by.vadim_churun.ordered.speechman2.db.entities

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.objs.HistoryStatus


/** Association between [Person] and [Product]. **/
@Entity(
    tableName = "Orders",
    primaryKeys = ["person", "product"] )
class Order(
    @ColumnInfo(name="person", index=true) val personID: Int,

    @ColumnInfo(name="product", index=true) val productID: Int,

    @ColumnInfo(name="history") val historyStatus: HistoryStatus,

    @ColumnInfo(name="isDeleted", index=true) val isLogicallyDeleted: Boolean
)