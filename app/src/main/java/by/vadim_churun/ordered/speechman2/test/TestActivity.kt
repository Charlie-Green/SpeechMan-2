package by.vadim_churun.ordered.speechman2.test

import androidx.appcompat.app.AppCompatActivity
import android.os.*
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.Money
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData
import by.vadim_churun.ordered.speechman2.model.warning.*
import by.vadim_churun.ordered.speechman2.remote.lack.SeminarCityLack
import kotlinx.android.synthetic.main.test_activity.*
import kotlin.concurrent.thread


class TestActivity: AppCompatActivity()
{
    private var stopped = true

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.test_activity)
    }

    override fun onStart()
    {
        super.onStart()
        stopped = false

        thread(start = true) {
            val data = RemoteData(
                requestID = 0,

                entities = listOf(
                    Person(7, "Melinda", null)
                ),

                lacks = listOf(
                    SeminarCityLack(null,
                        "Alalia",
                        "85 West-Street",
                        "",
                        Seminar.CostingStrategy.DATE,
                        false
                    )
                ),

                warnings = listOf(
                    PersonNameExistsWarning(11, "Suzy", 3),
                    SeminarNameAndCityExistWarning(
                        88,
                        "Tutorillo",
                        "Washington",
                        "17A, White Street",
                        "",
                        Seminar.CostingStrategy.FIXED,
                        true
                    ),
                    ProductNameExistsWarning(15, "Speechy Box", Money(177f, "RUR"), 3, 5, false)
                )
            )

            Handler(Looper.getMainLooper()).post {
                filePager.adapter = TestPagerAdapter(
                    this, super.getSupportFragmentManager(), data)
            }
        }
    }

    override fun onStop()
    {
        stopped = true
        super.onStop()
    }
}
