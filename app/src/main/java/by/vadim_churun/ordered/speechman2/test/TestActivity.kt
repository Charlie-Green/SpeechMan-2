package by.vadim_churun.ordered.speechman2.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.*
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManActivity
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.*
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData
import by.vadim_churun.ordered.speechman2.model.warning.*
import by.vadim_churun.ordered.speechman2.remote.lack.*
import by.vadim_churun.ordered.speechman2.repo.RemoteRepository
import java.util.Calendar
import kotlinx.android.synthetic.main.test_activity.*
import kotlin.concurrent.thread


class TestActivity: AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.test_activity)
        super.deleteDatabase("speech.db")

        fabSpeechMan.setOnClickListener {
            Intent(super.getApplicationContext(), SpeechManActivity::class.java).also {
                super.startActivity(it)
            }
        }

        thread(start = true) {
            val inData = RemoteData(
                requestID = 1,

                entities = listOf(
                    Person(6, "Sinorita", null)
                ),

                lacks = listOf(),

                warnings = listOf()
            )

            val outData = RemoteRepository(super.getApplicationContext())
                .HANDLE_REMOTE_DATA_DEBUG(inData.toBuilder())

            Handler(Looper.getMainLooper()).post {
                filePager.adapter = TestPagerAdapter(
                    this, super.getSupportFragmentManager(), outData)
            }
        }
    }
}
