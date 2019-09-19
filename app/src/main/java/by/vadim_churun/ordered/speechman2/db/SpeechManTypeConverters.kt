package by.vadim_churun.ordered.speechman2.db

import android.net.Uri
import androidx.room.TypeConverter
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.db.objs.*
import java.util.Calendar


class SpeechManTypeConverters
{
    @TypeConverter
    fun uriToString(uri: Uri?): String?
        = uri?.toString()

    @TypeConverter
    fun stringToUri(str: String?): Uri?
        = str?.let { Uri.parse(it) }


    @TypeConverter
    fun moneyToString(money: Money?): String?
        = money?.toString()

    @TypeConverter
    fun stringToMoney(str: String?): Money?
        = str?.let { Money.parse(it) }


    @TypeConverter
    fun costingStrategyToInt(cs: Seminar.CostingStrategy): Int
        = cs.ordinal

    @TypeConverter
    fun intToCostingStrategy(int: Int): Seminar.CostingStrategy
        = Seminar.CostingStrategy.values()[int]


    @TypeConverter
    fun historyStatusToInt(hs: HistoryStatus): Int
        = hs.ordinal

    @TypeConverter
    fun intToHistoryStatus(int: Int): HistoryStatus
        = HistoryStatus.values()[int]


    @TypeConverter
    fun calendarToLong(cal: Calendar?): Long?
        = cal?.timeInMillis

    @TypeConverter
    fun longToCalendar(long: Long?): Calendar?
    {
        long ?: return null
        return Calendar.getInstance().apply { timeInMillis = long }
    }
}