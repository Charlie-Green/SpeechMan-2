package by.vadim_churun.ordered.speechman2.dests.remote

import android.net.*
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.dialogs.remote.IPDialog
import by.vadim_churun.ordered.speechman2.model.exceptions.UnknownResponseSpeechManException
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.remote.connect.SpeechManServerException
import by.vadim_churun.ordered.speechman2.remote.xml.SpeechManXmlException
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Observable
import io.reactivex.disposables.*
import kotlinx.android.synthetic.main.remote_destination.*


class RemoteDestination: SpeechManFragment(R.layout.remote_destination)
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION:

    companion object
    {
        private val LOGTAG = RemoteDestination::class.java.simpleName
        private const val KEY_REQUEST_ID = "requestID"
    }


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
        Log.e(LOGTAG, "${thr.javaClass.name}: ${thr.message}")
        // We know that this request resulted into an error and don't expect results anymore.
        requestID = null

        prbDataLoad.visibility = View.GONE
        tvLog.text = ""
        var messageResId = R.string.msg_remote_unknown_error
        if(thr is SpeechManXmlException)
            messageResId = R.string.msg_remote_xml_error
        else if(thr.javaClass.name.startsWith("java.net."))
            messageResId = R.string.msg_remote_network_error
        else if(thr is SpeechManServerException &&
            thr.reason == SpeechManServerException.Reason.UNKNOWN_REMOTE_ACTION )
            messageResId = R.string.msg_unknown_remote_action
        else if(thr is SpeechManServerException &&
            thr.reason == SpeechManServerException.Reason.NO_DATA )
            messageResId = R.string.msg_server_no_data
        else if(thr is SpeechManServerException &&
            thr.reason == SpeechManServerException.Reason.IO_EXCEPTION )
            messageResId = R.string.msg_server_ioexception
        else if(thr is UnknownResponseSpeechManException)
            messageResId = R.string.msg_unknown_server_response

        AlertDialog.Builder(super.requireContext())
            .setMessage(messageResId)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun notifyConnectionOpened()
    {
        prbDataLoad.visibility = View.VISIBLE
        tvLog.setTextColor(colorText)
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

    private fun notifyXmlParsed() {
        tvLog.setTextColor(colorText)
        tvLog.setText(R.string.msg_data_pulled)
    }

    private fun requestRemoteAction()
    {
        prbDataLoad.visibility = View.VISIBLE
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
                this@RemoteDestination.viewModel
                    .actionSubject
                    .onNext( SpeechManAction.ApplyAvailableNetwork(network) )
            }

            override fun onLost(network: Network)
            {
                tvLog.setTextColor(colorError)
                tvLog.setText(R.string.msg_lost_network)
            }
        }
        connectMan.requestNetwork(netRequest, netCallback!!)
    }

    private fun applyAvailableNetwork()
    {
        tvLog.text = ""
        val dialogArgs = Bundle()
        dialogArgs.putInt(IPDialog.KEY_REMOTE_ACTION, remoteAction.ordinal)
        IPDialog().apply {
            arguments = dialogArgs
            show(this@RemoteDestination.requireFragmentManager(), null)
        }
        prbDataLoad.visibility = View.GONE
    }

    private fun unregisterNetCallback()
    {
        tvLog.text = ""
        netCallback?.also { ContextCompat
            .getSystemService(super.requireContext(), ConnectivityManager::class.java)!!
            .unregisterNetworkCallback(it)
        }
        netCallback = null
        prbDataLoad.visibility = View.GONE
    }

    private fun navigateNext(rd: RemoteData)
    {
        super.viewModel.keepRemoteData(rd)

        val actionID: Int
        if(rd.lacks.isNotEmpty()) {
            actionID = R.id.actRemoteToLacks
        } else if(rd.warnings.isNotEmpty()) {
            actionID = R.id.actRemoteToWarnings
        } else {
            actionID = R.id.actToPeople
            SpeechManAction.ShowMessage(
                false, super.getResources(), R.string.msg_import_finished
            ).also {
                super.viewModel
                    .actionSubject
                    .onNext(it)
            }
        }

        findNavController().navigate(actionID)
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()
    private var isRemoteDataSubscribed = false

    private fun subscribeAvailableNetwork()
        = super.viewModel
            .actionSubject
            .observeOn(AndroidSchedulers.mainThread())
            .filter { action ->
                action is SpeechManAction.ApplyAvailableNetwork
            }.doOnNext { _ ->
                applyAvailableNetwork()
            }.subscribe()

    private fun subscribeSyncRequest()
        = super.viewModel
            .actionSubject
            .observeOn(AndroidSchedulers.mainThread())
            .filter { action ->
                action is SpeechManAction.RequestSync
            }.doOnNext { action ->
                requestID = (action as SpeechManAction.RequestSync).request.requestID
            }.subscribe()

    private fun subscribeSyncResponse()
        = super.viewModel
            .createSyncResponseObservable()
            .filter { response ->
                response.requestID == requestID
            }.doOnNext { response ->
                when(response.action)
                {
                    SyncResponse.ProgressStatus.CONNECTION_OPENED
                        -> { notifyConnectionOpened() }
                    SyncResponse.ProgressStatus.DATA_PUSHED
                        -> { notifyDataPushed(); unregisterNetCallback() }
                    SyncResponse.ProgressStatus.XML_PARSED
                        -> { notifyXmlParsed() }
                }
            }.subscribe()

    private fun subscribeRemoteData(): Disposable?
    {
        if(isRemoteDataSubscribed) return null
        isRemoteDataSubscribed = true
        return super.viewModel
            .createRemoteDataObservable()
            .onErrorResumeNext { thr: Throwable ->
                unregisterNetCallback()
                handleError(thr)
                isRemoteDataSubscribed = false
                Observable.empty()
            }.filter { rd ->
                rd.requestID == requestID
            }.doOnNext { rd ->
                prbDataLoad.visibility = View.GONE
                navigateNext(rd)
            }.subscribe()
    }

    private fun clearDisposable()
    {
        disposable.clear()
        isRemoteDataSubscribed = false
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        if(savedInstanceState?.containsKey(KEY_REQUEST_ID) == true)
            requestID = savedInstanceState.getInt(KEY_REQUEST_ID)

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
        tvLog.text = ""
        disposable.add(subscribeSyncRequest())
        disposable.add(subscribeSyncResponse())
        subscribeRemoteData()?.also { disposable.add(it) }
        disposable.add(subscribeAvailableNetwork())
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        requestID?.also { outState.putInt(KEY_REQUEST_ID, it) }
    }

    override fun onStop()
    {
        unregisterNetCallback()
        clearDisposable()
        super.onStop()
    }
}