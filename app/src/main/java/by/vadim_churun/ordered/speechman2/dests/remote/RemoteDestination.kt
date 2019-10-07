package by.vadim_churun.ordered.speechman2.dests.remote

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Looper
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.model.objects.SyncRequest
import by.vadim_churun.ordered.speechman2.model.objects.SyncResponse
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.remote_destination.*


class RemoteDestination: SpeechManFragment(R.layout.remote_destination)
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // STYLING:

    private var colorText: Int = 0
    private var colorError: Int = 0

    private fun initColors()
    {
        val typval = TypedValue()
        val theme = super.requireContext().theme
        theme.resolveAttribute(android.R.attr.textColor, typval, true)
        colorText = typval.data
        colorError = ContextCompat.getColor(super.requireContext(), R.color.error)
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var requestID: Int? = null
    private var netCallback: ConnectivityManager.NetworkCallback? = null


    private fun notifyConnectionOpened()
    {
        tvLog.setText(R.string.msg_connection_opened)
    }

    private fun requestRemoteAction(action: SyncRequest.RemoteAction)
    {
        tvLog.setTextColor(colorText)
        tvLog.setText(R.string.msg_waiting_for_network)

        val netRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val connectMan = ContextCompat.getSystemService(
            super.requireContext(), ConnectivityManager::class.java )!!
        netCallback = netCallback ?: object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network)
            {
                // TODO: Show IP dialog
            }

            override fun onUnavailable()
            {
                tvLog.setTextColor(colorError)
                tvLog.setText(R.string.msg_lost_network)
            }
        }
        connectMan.requestNetwork(netRequest, netCallback!!)
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeSyncResponse()
        = super.viewModel.createSyncResponseObservable()
            .filter { response ->
                response.action == SyncResponse.ProgressStatus.CONNECTION_OPENED
            }.doOnNext { response ->
                if(Looper.myLooper() != Looper.getMainLooper())
                    throw Exception("Received SyncResponse in background.")
                notifyConnectionOpened()
            }.subscribe()


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        initColors()

        val onImportRequested = View.OnClickListener {
            requestRemoteAction(SyncRequest.RemoteAction.IMPORT)
        }
        cardvImport.setOnClickListener(onImportRequested)
        grltImport.setOnClickListener(onImportRequested)

        val onExportRequested = View.OnClickListener {
            requestRemoteAction(SyncRequest.RemoteAction.EXPORT)
        }
        cardvExport.setOnClickListener(onExportRequested)
        grltExport.setOnClickListener(onExportRequested)
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(subscribeSyncResponse())
    }

    override fun onStop()
    {
        netCallback?.also { ContextCompat
            .getSystemService(super.requireContext(), ConnectivityManager::class.java )!!
            .unregisterNetworkCallback(it)
        }
        disposable.clear()
        super.onStop()
    }
}