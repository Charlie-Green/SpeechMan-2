package by.vadim_churun.ordered.speechman2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
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
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        private val STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val STORAGE_PERMISSION_REQCODE = 311
        private const val SELECT_IMAGE_REQCODE = 971
    }


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

    private fun setupPopUp()
    {
        val params = navHost.view!!.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = object: CoordinatorLayout.Behavior<View>() {
            override fun layoutDependsOn
                        (parent: CoordinatorLayout, child: View, dependency: View): Boolean
                    = dependency is Snackbar.SnackbarLayout    // Pop-up in response to a Snackbar.

            override fun onDependentViewChanged
                        (parent: CoordinatorLayout, child: View, dependency: View): Boolean
            {
                val childParams = child.layoutParams as CoordinatorLayout.LayoutParams
                childParams.bottomMargin = dependency.height.minus(dependency.translationY).toInt()
                child.layoutParams = childParams
                return true   // Position was changed.
            }
        }
        navHost.view!!.layoutParams = params
    }

    private fun showMessage(messageAction: SpeechManAction.ShowMessage)
    {
        val dur = if(messageAction.showAsError) 4000 else 2000
        Snackbar.make(cdltContent, messageAction.message, dur).apply {
            view.setBackgroundColor( if(messageAction.showAsError) colorErrorBack else colorFore )
            view.findViewById<TextView>(R.id.snackbar_text).setTextColor(
                if(messageAction.showAsError) colorError else colorBack )
            show()
        }
    }

    private fun checkStoragePermission(): Boolean
        = ActivityCompat
            .checkSelfPermission(this, STORAGE_PERMISSION)
            .equals(PackageManager.PERMISSION_GRANTED)

    private fun requestStoragePermission()
        = ActivityCompat.requestPermissions(this,
            kotlin.arrayOf(STORAGE_PERMISSION), STORAGE_PERMISSION_REQCODE )

    private fun requestImage()
    {
        Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "image/*"
        }.also {
            super.startActivityForResult(it, SELECT_IMAGE_REQCODE)
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

                R.id.miSeminars -> {
                    navController.navigate(R.id.actToSeminars)
                }
            }

            drawerLayout.closeDrawers()
            true
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            val itemID: Int
            val destID = destination.id
            if(destID == R.id.destPeopleList ||
                destID == R.id.destPersonDetail ||
                destID == R.id.destPersonAppointments ||
                destID == R.id.destAddPersonAppoint )
                itemID = R.id.miPeople
            else if(destID == R.id.destSeminarsList ||
                destID == R.id.destSeminarDetail ||
                destID == R.id.destAddSeminar ||
                destID == R.id.destEditSeminar ||
                destID == R.id.destEditParticipants )
                itemID = R.id.miSeminars
            else
                throw Exception("Unknown destination")
            navDrawer.setCheckedItem(itemID)
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
                (action is SpeechManAction.ShowMessage) ||
                (action is SpeechManAction.SelectImage) ||
                (action == SpeechManAction.NavigateBack)
            }.doOnNext { action ->
                when(action)
                {
                    is SpeechManAction.ShowMessage -> {
                        showMessage(action)
                    }

                    is SpeechManAction.SelectImage -> {
                        if(checkStoragePermission())
                            requestImage()
                        else
                            requestStoragePermission()
                    }

                    SpeechManAction.NavigateBack -> {
                        findNavController(R.id.navHost).navigateUp()
                    }
                }
            }
            .subscribe()


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.speechman_activity)
        setupPopUp()
        initColors()
        setupNavigation()
        disposable.add(connectActionsChannel())
    }

    override fun onRequestPermissionsResult
    (requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if(requestCode != STORAGE_PERMISSION_REQCODE) return
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            requestImage()
        else
            showMessage(SpeechManAction.ShowMessage(
                true, super.getString(R.string.msg_need_permission_for_image)) )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, dataIntent)
        if(requestCode != SELECT_IMAGE_REQCODE
            || resultCode != Activity.RESULT_OK)
            return

        val uri = dataIntent!!.data!!
        val flags = dataIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
        super.getContentResolver().takePersistableUriPermission(uri, flags)
        val vm = ViewModelProviders.of(this).get(SpeechManViewModel::class.java)
        vm.keepAction( SpeechManAction.DecodeImages(vm.nextImageDecodeID, listOf(uri)) )
    }

    override fun onDestroy()
    {
        super.onDestroy()
        disposable.clear()
    }
}