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

    /** Selects both deleted and undeleted [Appointment]s. **/
    @Query("select * from Appointments where person=:personID")
    fun getAllAppointmentsForPerson(personID: Int): List<Appointment>

    /** Selects both deleted and undeleted [Orders]. **/
    @Query("select * from Orders where person=:personID")
    fun getAllOrdersForPerson(personID: Int): List<Order>

    /** Can be used for both insertion and logical deletion. **/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdateAppointments(appoints: List<Appointment>)

    /** Can be used for both insertion and logical deletion. **/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdateOrders(orders: List<Order>)

    @Delete
    fun deleteAppointments(appoints: List<Appointment>)

    @Delete
    fun deleteOrders(orders: List<Order>)
}