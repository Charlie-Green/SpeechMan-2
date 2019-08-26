package by.vadim_churun.ordered.speechman2.db.objs

import androidx.room.Embedded
import by.vadim_churun.ordered.speechman2.db.entities.*


class AppointedSeminar(
    @Embedded(prefix = "sem_")
    val seminar: Seminar,

   @Embedded(prefix = "appoint_")
   val appoint: Appointment
)