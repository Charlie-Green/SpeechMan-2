package by.vadim_churun.ordered.speechman2.db.daos

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.*
import io.reactivex.*


@Dao
interface SeminarsDAO
{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SELECT:

    @Query("select * from Seminars where Seminars.id=:seminarID")
    fun get(seminarID: Int): Seminar

    @Query("select * from Seminars where Seminars.id=:seminarID")
    fun getRx(seminarID: Int): Observable<Seminar>

    @Query("select * from Seminars where name=:name and city=:city and isDeleted=0")
    fun getByNameAndCity(name: String, city: String): List<Seminar>

    @Query("select :seminarID as id, " +
            "max(Seminars.name) as name, " +
            "max(Seminars.image) as image, " +
            "max(Seminars.city) as city, " +
            "max(Seminars.isDeleted) as isDeleted, " +
            "min(SemDays.start) as start " +
            "from Seminars left join SemDays on Seminars.id=SemDays.seminar " +
            "where Seminars.id=:seminarID" )
    fun getHeader(seminarID: Int): Observable<SeminarHeader>

    @Query("select Seminars.id as id, " +
           "max(Seminars.name) as name, " +
           "max(Seminars.image) as image, " +
           "max(Seminars.city) as city, " +
           "0 as isDeleted, " +
           "min(SemDays.start) as start " +
           "from Seminars left join SemDays on Seminars.id=SemDays.seminar " +
           "group by Seminars.id " +
           "having Seminars.isDeleted=0" )
    fun getHeaders(): Observable< List<SeminarHeader> >

    /** Selects only those [Appointment]s which haven't been logically deleted. **/
    @Query("select People.id as person_id, " +
           "People.name as person_name, " +
           "People.type as person_type, " +
           "Appointments.person as appoint_person, " +
           "Appointments.seminar as appoint_seminar, " +
           "Appointments.paid as appoint_paid, " +
           "Appointments.cost as appoint_cost, " +
           "Appointments.history as appoint_history, " +
           "Appointments.isDeleted as appoint_isDeleted " +
           "from People inner join Appointments on People.id=Appointments.person " +
           "where Appointments.seminar=:seminarID and Appointments.isDeleted=0" )
    fun getParticipants(seminarID: Int): Observable< List<Participant> >

    /** Selects [Appointment]s regardless of their logical deletion status. **/
    @Query("select People.id as person_id, " +
           "People.name as person_name, " +
           "People.type as person_type, " +
           "Appointments.person as appoint_person, " +
           "Appointments.seminar as appoint_seminar, " +
           "Appointments.paid as appoint_paid, " +
           "Appointments.cost as appoint_cost, " +
           "Appointments.history as appoint_history, " +
           "Appointments.isDeleted as appoint_isDeleted " +
           "from People inner join Appointments on People.id=Appointments.person " +
           "where Appointments.seminar=:seminarID" )
    fun getAllParticipants(seminarID: Int): Observable< List<Participant> >

    @Query("select * from SemDays where seminar=:seminarID")
    fun getDays(seminarID: Int): Observable< List<SemDay> >

    @Query("select * from SemCosts where id=:costID")
    fun rawGetCost(costID: Int): SemCost

    @Query("select * from SemCosts where seminar=:seminarID")
    fun getCosts(seminarID: Int): Observable< List<SemCost> >


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INSERT, UPDATE, DELETE:

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdate(seminar: Seminar): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdateRx(seminar: Seminar): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdateDays(days: List<SemDay>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdateCosts(costs: List<SemCost>)

    @Delete
    fun delete(seminar: Seminar)

    @Delete
    fun deleteDays(days: List<SemDay>)

    @Delete
    fun deleteCosts(costs: List<SemCost>)
}