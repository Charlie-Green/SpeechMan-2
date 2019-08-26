package by.vadim_churun.ordered.speechman2.db.entities

import androidx.room.*


@Entity(tableName = "PersonTypes")
class PersonType(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id", index = true) val ID: Int?,

    @ColumnInfo(name="label") val label: String
)