package by.vadim_churun.ordered.speechman2.db.entities

import androidx.room.*


@Entity(tableName = "People")
class Person(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id", index = true) val ID: Int?,

    @ColumnInfo(name="name") val name: String,

    @ColumnInfo(name="type") val personTypeID: Int?
)