package by.vadim_churun.ordered.speechman2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManViewModel
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.speechman_activity.*


class SpeechManActivity: AppCompatActivity()
{
    private val LOGTAG = SpeechManActivity::class.java.simpleName

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // FIELDS AND HELP METHODS:

    private var colorBack = 0
    private var colorFore = 0
    private var colorError = 0
    private var colorErrorBack = 0

    private fun initColors()
    {
        val typval = TypedValue()
        super.getTheme().resolveAttribute(android.R.attr.colorBackground, typval, true)
        colorBack = typval.data
        super.getTheme().resolveAttribute(android.R.attr.colorForeground, typval, true)
        colorFore = typval.data
        colorError = ContextCompat.getColor(this, R.color.error)
        colorErrorBack = ContextCompat.getColor(this, R.color.errorBackground)
    }

    private fun showMessage(messageAction: SpeechManAction.ShowMessage)
    {
        val dur = if(messageAction.showAsError) 4000 else 2000
        Snackbar.make(cdltRoot, messageAction.message, dur).apply {
            view.setBackgroundColor( if(messageAction.showAsError) colorErrorBack else colorFore )
            view.findViewById<TextView>(R.id.snackbar_text).setTextColor(
                if(messageAction.showAsError) colorError else colorBack )
            show()
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MANAGING NAVIGATION:

    private fun setupNavigation()
    {
        val navController = findNavController(R.id.navHost)
        navDrawer.setCheckedItem(R.id.miPeople)

        navDrawer.setNavigationItemSelectedListener { selectedItem ->
            if(navDrawer.checkedItem == selectedItem)
                return@setNavigationItemSelectedListener true

            when(selectedItem.itemId)
            {
                R.id.miPeople -> {
                    navController.navigate(R.id.actToPeople)
                }
            }

            drawerLayout.closeDrawers()
            true
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.parent?.id)
            {
                R.id.navdrawer_people_subgraph -> {
                    navDrawer.setCheckedItem(R.id.miPeople)
                }
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun connectActionsChannel()
        = ViewModelProviders.of(this)
            .get(SpeechManViewModel::class.java)
            .actionSubject
            .observeOn(AndroidSchedulers.mainThread())
            .filter { action ->
                action is SpeechManAction.ShowMessage
            }.doOnNext { action ->
                showMessage(action as SpeechManAction.ShowMessage)
            }
            .subscribe()


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.speechman_activity)
        initColors()
        setupNavigation()
        disposable.add(connectActionsChannel())
    }

    override fun onDestroy()
    {
        super.onDestroy()
        disposable.clear()
    }
}
