package by.vadim_churun.ordered.speechman2.db.daos

import androidx.room.*
import by.vadim_churun.ordered.speechman2.db.entities.Product


@Dao
interface ProductsDAO
{
    @Query("select * from Products where name=:name")
    fun getByName(name: String): List<Product>

    /** Can be used for both updating and logical deletion. **/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdate(product: Product): Long
}