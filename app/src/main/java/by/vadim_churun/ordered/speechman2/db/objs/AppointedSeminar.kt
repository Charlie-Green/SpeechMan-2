package by.vadim_churun.ordered.speechman2.db.objs

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.entities.*
import java.util.Calendar


class AppointedSeminar(
    @Embedded(prefix = "sem_")
    val seminar: Seminar,

   @Embedded(prefix = "appoint_")
   val appoint: Appointment,

    @ColumnInfo(name = "start")
    val start: Calendar?
)