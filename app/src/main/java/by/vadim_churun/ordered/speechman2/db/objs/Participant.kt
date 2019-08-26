package by.vadim_churun.ordered.speechman2.db.objs

import androidx.room.Embedded
import by.vadim_churun.ordered.speechman2.db.entities.*


class Participant(
    @Embedded(prefix = "person_")
    val person: Person,

    @Embedded(prefix = "appoint_")
    val appoint: Appointment
)