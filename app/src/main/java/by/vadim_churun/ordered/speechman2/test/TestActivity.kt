package by.vadim_churun.ordered.speechman2.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import by.vadim_churun.ordered.speechman2.R
import kotlinx.android.synthetic.main.test_activity.*


class TestActivity: AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.test_activity)
        filePager.adapter = TestPagerAdapter(this, super.getSupportFragmentManager())
    }
}
