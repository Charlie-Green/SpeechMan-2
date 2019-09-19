package by.vadim_churun.ordered.speechman2.db.objs

import android.net.Uri
import androidx.room.ColumnInfo
import java.util.Calendar


class SeminarHeader(
    @ColumnInfo(name="id") val ID: Int,
    @ColumnInfo(name="name") val name: String,
    @ColumnInfo(name="image") val imageUri: Uri?,
    @ColumnInfo(name="city") val city: String,
    @ColumnInfo(name="isDeleted") val isLogicallyDeleted: Boolean,
    @ColumnInfo(name="start") val start: Calendar?
)