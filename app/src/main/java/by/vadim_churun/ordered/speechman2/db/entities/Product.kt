package by.vadim_churun.ordered.speechman2.db.entities

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.objs.Money


@Entity(tableName = "Products")
class Product(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id") val ID: Int,

    @ColumnInfo(name="name") val name: String,

    @ColumnInfo(name="cost") val cost: Money,

    @ColumnInfo(name="countBoxes") val countBoxes: Int,

    @ColumnInfo(name="countCase") val countCase: Int
)