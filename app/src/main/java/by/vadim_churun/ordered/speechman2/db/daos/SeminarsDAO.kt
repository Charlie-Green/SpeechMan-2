package by.vadim_churun.ordered.speechman2.db.daos

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.objs.Participant
import io.reactivex.*


@Dao
interface SeminarsDAO
{
    /** Selects [Appointment]s regardless of their logical deletion status. **/
    @Query("select People.id as person_id, People.name as person_name, People.type as person_type, " +
            "Appointments.person as appoint_person, Appointments.seminar as appoint_seminar, " +
            "Appointments.cost as appoint_cost, Appointments.history as appoint_history, " +
            "Appointments.isDeleted as appoint_isDeleted " +
            "from People inner join Appointments on People.id=Appointments.person " +
            "where Appointments.seminar=:seminarID" )
    fun getParticipants(seminarID: Int): Observable< List<Participant> >
}