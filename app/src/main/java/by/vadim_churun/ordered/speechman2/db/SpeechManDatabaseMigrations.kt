package by.vadim_churun.ordered.speechman2.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


/** Produces [Migration] objects for the application database. **/
internal object SpeechManDatabaseMigrations
{
    /** Application versions. **/
    enum class Version {
        V2_0_ALPHA1,
        V2_0_BETA1,
        V2_1_ALPHA1,
        V2_1_BETA1
    }


    private fun getEmptyMigration(from: Int, to: Int)
        = object: Migration(from, to) {
            override fun migrate(database: SupportSQLiteDatabase)
            { /* This Migration is returned if data model didn't change. */ }
        }

    private val MIGRATION_2_3
        = object: Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("alter table Orders add paid text not null default '0.00 BYN'")

                database.execSQL("pragma foreign_keys=off")
                database.execSQL("alter table Products add isDeleted integer not null default 0")
                database.execSQL("alter table Products rename to Products_temp")
                database.execSQL(
                    "create table Products(" +
                        "id integer primary key autoincrement, " +
                        "name text not null, " +
                        "cost text not null, " +
                        "countBoxes integer not null, " +
                        "countCase integer not null, " +
                        "isDeleted integer not null)"
                )
                database.execSQL("insert into Products select * from Products_temp")
                database.execSQL("drop table Products_temp")
                database.execSQL("pragma foreign_keys=on")
            }
        }


    fun get(from: Version, to: Version): Migration {
        if(from == Version.V2_0_ALPHA1 && to == Version.V2_0_BETA1)
            return getEmptyMigration(1, 2)
        if(from == Version.V2_0_BETA1 && to == Version.V2_1_ALPHA1)
            return MIGRATION_2_3
        if(from == Version.V2_1_ALPHA1 && to == Version.V2_1_BETA1)
            return getEmptyMigration(3, 4)
        throw IllegalArgumentException(
            "Direct migration from ${from.name} to ${to.name} is not supported." )
    }
}