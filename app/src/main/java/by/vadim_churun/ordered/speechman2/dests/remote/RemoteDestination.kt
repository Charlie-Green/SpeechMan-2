package by.vadim_churun.ordered.speechman2.dests.remote

import android.graphics.Color
import android.net.*
import android.os.Bundle
import android.os.Looper
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.dialogs.remote.IPDialog
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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
    private lateinit var remoteAction: SyncRequest.RemoteAction


    private fun handleError(thr: Throwable)
    {
        prbDataLoad.visibility = View.GONE
        tvLog.setTextColor(Color.RED)
        tvLog.text = "${thr.javaClass.simpleName}: ${thr.message}"
    }

    private fun notifyConnectionOpened()
    {
        prbDataLoad.visibility = View.VISIBLE
        tvLog.setText(R.string.msg_connection_opened)
    }

    private fun notifyDataPushed()
    {
        SpeechManAction.ShowMessage(false,
            super.getResources(),
            R.string.msg_data_pushed
        ).also {
            super.viewModel.actionSubject.onNext(it)
        }
    }

    private fun notifyXmlParsed()
    {
        tvLog.setText(colorText)
        tvLog.setText(R.string.msg_data_pulled)
    }

    private fun requestRemoteAction()
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
                tvLog.text = ""
                val dialogArgs = Bundle()
                dialogArgs.putInt(IPDialog.KEY_REMOTE_ACTION, remoteAction.ordinal)
                IPDialog().apply {
                    arguments = dialogArgs
                    show(this@RemoteDestination.requireFragmentManager(), null)
                }
            }

            override fun onLost(network: Network)
            {
                tvLog.setTextColor(colorError)
                tvLog.setText(R.string.msg_lost_network)
            }
        }
        connectMan.requestNetwork(netRequest, netCallback!!)
    }

    private fun unregisterNetCallback()
    {
        netCallback?.also {
            ContextCompat.getSystemService(super.requireContext(), ConnectivityManager::class.java)!!
                .unregisterNetworkCallback(it)
        }
        netCallback = null
    }

    private fun navigateDataDestination()
    {
        // TODO
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()
    private var isRemoteDataSubscribed = false

    private fun subscribeSyncResponse()
        = super.viewModel.createSyncResponseObservable()
            .doOnNext { response ->
                if(Looper.myLooper() != Looper.getMainLooper())
                    throw Exception("Received SyncResponse in background.")
                when(response.action)
                {
                    SyncResponse.ProgressStatus.CONNECTION_OPENED -> notifyConnectionOpened()
                    SyncResponse.ProgressStatus.DATA_PUSHED       -> notifyDataPushed()
                    SyncResponse.ProgressStatus.XML_PARSED        -> notifyXmlParsed()
                }
            }.subscribe()

    private fun subscribeRemoteData(): Disposable?
    {
        if(isRemoteDataSubscribed) return null
        isRemoteDataSubscribed = true
        return super.viewModel.createRemoteDataObservable()
            .onErrorResumeNext { thr: Throwable ->
                unregisterNetCallback()
                handleError(thr)
                isRemoteDataSubscribed = false
                Observable.empty()
            }.doOnNext { rd ->
                prbDataLoad.visibility = View.GONE
                navigateDataDestination()
            }.subscribe()
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        initColors()

        val onImportRequested = View.OnClickListener {
            subscribeRemoteData()?.also { disposable.add(it) }
            remoteAction = SyncRequest.RemoteAction.IMPORT
            requestRemoteAction()
        }
        cardvImport.setOnClickListener(onImportRequested)
        grltImport.setOnClickListener(onImportRequested)

        val onExportRequested = View.OnClickListener {
            remoteAction = SyncRequest.RemoteAction.EXPORT
            requestRemoteAction()
        }
        cardvExport.setOnClickListener(onExportRequested)
        grltExport.setOnClickListener(onExportRequested)
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(subscribeSyncResponse())
        subscribeRemoteData()?.also { disposable.add(it) }
    }

    override fun onStop()
    {
        unregisterNetCallback()
        disposable.clear()
        super.onStop()
    }
}