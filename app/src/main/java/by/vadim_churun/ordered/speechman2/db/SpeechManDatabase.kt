package by.vadim_churun.ordered.speechman2.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import by.vadim_churun.ordered.speechman2.db.daos.*
import by.vadim_churun.ordered.speechman2.db.entities.*
import kotlin.concurrent.thread


@Database(
    entities = [Person::class,
        Seminar::class,
        Product::class,
        SemDay::class,
        SemCost::class,
        Appointment::class,
        Order::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(SpeechManTypeConverters::class)
abstract class SpeechManDatabase: RoomDatabase()
{
    abstract fun getPeopleDAO(): PeopleDAO
    abstract fun getSeminarsDAO(): SeminarsDAO
    abstract fun getAssociationsDAO(): AssociationsDAO


    companion object
    {
        private var instance: SpeechManDatabase? = null

        fun getInstance(appContext: Context)
            = instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    appContext, SpeechManDatabase::class.java, "speech.db" )
                    .addMigrations(SpeechManDatabaseMigrations.get(
                        SpeechManDatabaseMigrations.Version.V2_0_ALPHA1,
                        SpeechManDatabaseMigrations.Version.V2_0_BETA1
                    ), SpeechManDatabaseMigrations.get(
                        SpeechManDatabaseMigrations.Version.V2_0_BETA1,
                        SpeechManDatabaseMigrations.Version.V2_1_ALPHA1
                    )).build().also { instance = it }
            }
    }
}