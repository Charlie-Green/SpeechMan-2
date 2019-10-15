package by.vadim_churun.ordered.speechman2.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.adapters.DataLacksAdapter
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.db.objs.HistoryStatus
import by.vadim_churun.ordered.speechman2.db.objs.Money
import by.vadim_churun.ordered.speechman2.model.lack_info.DataLackInfo
import by.vadim_churun.ordered.speechman2.remote.lack.*
import kotlinx.android.synthetic.main.test_activity.*
import java.util.*


class TestActivity: AppCompatActivity()
{
    private val DEFAULT_SEM_CONTENT = "This seminar will tell you about different things. " +
            "Though there is a lot of stuff and it really covers a huge glory of topics, " +
            "you can find it extremely useful. And you will! Cause here out leading specialists " +
            "will tell you about biology, anatomy, health, speech teaching and blah-blah-blah. " +
            "Interesting? Then come just for $468456456.99"

    private val TEST_DATA = listOf<DataLack<*,*>>(
        AppointmentCostLack(15, 29, Money(0.00f, "BYN"), HistoryStatus.USUAL, false),
        AppointmentPurchaseLack(13, 29, Money(8000.00f, "RUR"), HistoryStatus.USUAL, false),
        ProductCostLack(null, "SuperToy", 80, 20, false),
        AppointmentMoneyLack(13, 22, HistoryStatus.USUAL, false),
        OrderPurchaseLack(15, 34, HistoryStatus.USUAL, false),
        ProductCostLack(31, "MagicBox", 4, 0, false),
        SemCostMoneyLack(23, 0, Calendar.getInstance().apply { timeInMillis = Long.MIN_VALUE }),
        SemCostMoneyLack(24, 20, Calendar.getInstance().apply { timeInMillis = Long.MIN_VALUE }),
        SemCostMoneyLack(25, 0, Calendar.getInstance()),
        SemCostMoneyLack(26, 20, Calendar.getInstance().apply { timeInMillis = Long.MIN_VALUE }),
        SemCostMoneyLack(27, 20, Calendar.getInstance().apply { timeInMillis = Long.MIN_VALUE }),
        SeminarNameLack(null,
            "Washington",
            "43a, West-Avenu 5",
            DEFAULT_SEM_CONTENT,
            Seminar.CostingStrategy.FIXED,
            false
        ),
        SeminarCityLack(null,
            "Tutorillo+",
            "",
            DEFAULT_SEM_CONTENT,
            Seminar.CostingStrategy.PARTICIPANTS_DATE,
            false
        ),
        SeminarNameLack(22,
            "NY",
            "81-14, Abraham's street",
            "",
            Seminar.CostingStrategy.PARTICIPANTS,
            false
        ),
        SeminarCityLack(null,
            "Semmy",
            "79, Nowhere square",
            "",
            Seminar.CostingStrategy.DATE_PARTICIPANTS,
            false
        ),
        SeminarNameLack(null,
            "",
            "",
            "Some weird content.",
            Seminar.CostingStrategy.DATE,
            false
        )
    )

    private val LACK_INFOS = listOf<DataLackInfo?>(
        DataLackInfo.AppointmentInfo("Linda", "GreatestEverSem"),
        DataLackInfo.AppointmentInfo("Kyle", "GreatestEverSem"),
        null,
        DataLackInfo.AppointmentInfo("Linda", "Alalia Tutorial"),
        DataLackInfo.OrderInfo("Maria", "Kiddy-Meow"),
        null,
        DataLackInfo.SemCostInfo("Alalia Tutorial", Seminar.CostingStrategy.FIXED),
        DataLackInfo.SemCostInfo("Biologhy Seminar", Seminar.CostingStrategy.PARTICIPANTS),
        DataLackInfo.SemCostInfo("Authism", Seminar.CostingStrategy.DATE),
        DataLackInfo.SemCostInfo("Spiritism", Seminar.CostingStrategy.PARTICIPANTS_DATE),
        DataLackInfo.SemCostInfo("Speech Tutorial", Seminar.CostingStrategy.DATE_PARTICIPANTS),
        null,
        null,
        null,
        null,
        null
    )


    private fun setAdapter(lacks: List<DataLack<*, *>>)
    {
        recvTest.layoutManager = LinearLayoutManager(this)
        val newAdapter = DataLacksAdapter(
            this, super.getSupportFragmentManager(), lacks )
        recvTest.swapAdapter(newAdapter, true)
    }

    private fun setLackInfos(infos: List<DataLackInfo?>)
    {
        (recvTest.adapter as DataLacksAdapter).setLackInfos(infos)
    }


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.test_activity)

        setAdapter(TEST_DATA)
        buProvideInfos.setOnClickListener {
            setLackInfos(LACK_INFOS)
        }
    }
}
