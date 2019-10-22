package by.vadim_churun.ordered.speechman2.db.daos

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.entities.*


@Dao
interface AssociationsDAO
{
    /** Counts only those [Appointment]s which have not been logically deleted. **/
    @Query("select count(*) from Appointments where person=:personID and isDeleted=0")
    fun countAppointmentsForPerson(personID: Int): Int

    /** Counts only those [Order]s which have not been logically deleted. **/
    @Query("select count(*) from Orders where person=:personID and isDeleted=0")
    fun countOrdersForPerson(personID: Int): Int

    /** Counts only those [Appointment]s which have not been logically deleted. **/
    @Query("select count(*) from Appointments where seminar=:seminarID and isDeleted=0")
    fun countAppointmentsForSeminar(seminarID: Int): Int

    /** Selects only those [Appointment]s which have not been logically deleted. **/
    @Query("select * from Appointments where person=:personID and isDeleted=0")
    fun getAppointmentsForPerson(personID: Int): List<Appointment>

    /** Selects only those [Order]s which have not been logically deleted. **/
    @Query("select * from Orders where person=:personID and isDeleted=0")
    fun getOrdersForPerson(personID: Int): List<Order>

    /** Selects only those [Appointment]s which have not been logically deleted. **/
    @Query("select * from Appointments where seminar=:seminarID and isDeleted=0")
    fun getAppointmentsForSeminar(seminarID: Int): List<Appointment>

    @Query("select * from Appointments")
    fun getAllAppointments(): List<Appointment>

    /** Selects both deleted and undeleted [Appointment]s. **/
    @Query("select * from Appointments where person=:personID")
    fun getAllAppointmentsForPerson(personID: Int): List<Appointment>

    @Query("select * from Orders")
    fun getAllOrders(): List<Order>

    /** Selects both deleted and undeleted [Orders]. **/
    @Query("select * from Orders where person=:personID")
    fun getAllOrdersForPerson(personID: Int): List<Order>

    /** Can be used for both insertion and logical deletion. **/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdateAppointments(appoints: List<Appointment>)

    /** Can be used for both insertion and logical deletion. **/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdateOrders(orders: List<Order>)

    /** Physical deletion. **/
    @Delete
    fun deleteAppointments(appoints: List<Appointment>)

    /** Physical deletion. **/
    @Delete
    fun deleteOrders(orders: List<Order>)
}