package by.vadim_churun.ordered.speechman2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.speechman_activity.*


class SpeechManActivity: AppCompatActivity()
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MANAGING NAVIGATION:

    private fun setupNavigation()
    {
        navDrawer.setCheckedItem(R.id.miPeople)
        navDrawer.setNavigationItemSelectedListener { selectedItem ->
            if(navDrawer.checkedItem == selectedItem)
                return@setNavigationItemSelectedListener true

            // TODO: Navigate

            drawerLayout.closeDrawers()
            true
        }

        // TODO: React on destination change.
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.speechman_activity)
        setupNavigation()
    }
}
