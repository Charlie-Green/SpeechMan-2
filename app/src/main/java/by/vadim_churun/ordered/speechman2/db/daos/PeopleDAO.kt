package by.vadim_churun.ordered.speechman2.db.daos

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.AppointedSeminar
import io.reactivex.*


@Dao
interface PeopleDAO
{
    @Query("select * from People")
    fun get(): Observable< List<Person> >

    @Query("select * from People where id=:personID")
    fun get(personID: Int): Observable<Person>

    /** Selects [Appointment]s and [Seminar]s regardless of their logical delete status. **/
    @Query("select Appointments.person as appoint_person, Appointments.seminar as appoint_seminar, " +
        "Appointments.cost as appoint_cost, Appointments.history as appoint_history, " +
        "Appointments.isDeleted as appoint_isDeleted, Seminars.id as sem_id, Seminars.city as sem_city, " +
        "Seminars.address as sem_address, Seminars.content as sem_content, Seminars.image as sem_image, " +
        "Seminars.costing as sem_costing, Seminars.isDeleted as sem_isDeleted " +
        "from Appointments inner join Seminars on Appointments.seminar=Seminars.id " +
        "where Appointments.person=:personID" )
    fun getAppointedSeminars(personID: Int): Observable< List<AppointedSeminar> >

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdate(person: Person)

    @Delete
    fun delete(person: Person)
}