package by.vadim_churun.ordered.speechman2.db.daos

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.AppointedSeminar
import by.vadim_churun.ordered.speechman2.db.objs.SeminarHeader
import io.reactivex.*


@Dao
interface PeopleDAO
{
    @Query("select * from People")
    fun get(): Observable< List<Person> >

    @Query("select * from People where id=:personID")
    fun get(personID: Int): Observable<Person>

    @Query("select * from People where id=:personID")
    fun rawGet(personID: Int): Person

    @Query("select * from People")
    fun rawGet(): List<Person>

    ///** Selects [Appointment]s and [Seminar]s regardless of their logical deletion status. **/
    /** Selects only those [Appointment]s which haven't been logically deleted. **/
    @Query("select max(Appointments.person) as appoint_person, " +
           "max(Appointments.seminar) as appoint_seminar, " +
           "max(Appointments.paid) as appoint_paid, " +
           "max(Appointments.cost) as appoint_cost, " +
           "max(Appointments.history) as appoint_history, " +
           "max(Appointments.isDeleted) as appoint_isDeleted, " +
           "max(Seminars.id) as sem_id, " +
           "max(Seminars.name) as sem_name, " +
           "max(Seminars.city) as sem_city, " +
           "max(Seminars.address) as sem_address, " +
           "max(Seminars.content) as sem_content, " +
           "max(Seminars.image) as sem_image, " +
           "max(Seminars.costing) as sem_costing, " +
           "max(Seminars.isDeleted) as sem_isDeleted, " +
           "min(SemDays.start) as start " +
           "from Appointments inner join Seminars on Appointments.seminar=Seminars.id " +
           "left join SemDays on Seminars.id=SemDays.seminar " +
           "group by Seminars.id, Appointments.person " +
           "having Appointments.person=:personID and Appointments.isDeleted=0" )
    fun getAppointedSeminars(personID: Int): Observable< List<AppointedSeminar> >

    /** Selects all [SeminarHeader]s regardless of their logical deletion status. **/
    @Query("select Seminars.id as id, " +
           "max(Seminars.name) as name, " +
           "max(Seminars.image) as image, " +
           "max(Seminars.city) as city, " +
           "max(Seminars.isDeleted) as isDeleted, " +
           "min(SemDays.start) as start " +
           "from Seminars left join SemDays on Seminars.id=SemDays.seminar " +
           "group by Seminars.id " +
           "having not Seminars.id in " +
           "(select seminar from Appointments where person=:excludedPersonID)" )
    fun getSemHeadersNotForPerson(excludedPersonID: Int): Observable< List<SeminarHeader> >

    @Query("select * from People where name=:name")
    fun getByName(name: String): List<Person>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdate(person: Person): Long

    @Delete
    fun delete(person: Person)

    @Delete
    fun delete(people: List<Person>)
}