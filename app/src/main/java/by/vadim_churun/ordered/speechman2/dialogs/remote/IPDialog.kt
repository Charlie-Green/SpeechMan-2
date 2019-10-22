package by.vadim_churun.ordered.speechman2.dialogs.remote

import android.content.DialogInterface
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.model.objects.SyncRequest
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.ip_dialog.*


class IPDialog: SpeechManFragment(R.layout.ip_dialog)
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        const val KEY_REMOTE_ACTION = "action"
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private fun requestSync(action: SyncRequest.RemoteAction)
    {
        SyncRequest(super.viewModel.nextSyncRequestID, action, etIp.text.toString()).let {
            SpeechManAction.RequestSync(it)
        }.also {
            super.viewModel.actionSubject.onNext(it)
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribePersistedIp()
        = super.viewModel
            .createPersistedIpMaybe()
            .doOnSuccess { ip ->
                if(Looper.myLooper() != Looper.getMainLooper())
                    throw Exception("Retrieved persisted IP in background!")
                etIp.setText(ip)
            }.subscribe()

    private fun subscribeIpValidation()
        = super.viewModel
            .createIpValidationObservable()
            .doOnNext { validated ->
                if(Looper.myLooper() != Looper.getMainLooper())
                    throw Exception("Retrieved IP validation status in background!")
                if(validated)
                    super.dismiss()
                else
                    tvInvalidIp.visibility = View.VISIBLE
            }.subscribe()


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    private var shouldRestoreIp = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        shouldRestoreIp = (savedInstanceState == null)

        val actionOrdinal = super.getIntArgument(KEY_REMOTE_ACTION, null, "KEY_REMOTE_ACTION")
        val action = SyncRequest.RemoteAction.values()[actionOrdinal]
        buSubmit.setOnClickListener { requestSync(action) }
    }

    override fun onStart()
    {
        super.onStart()
        if(shouldRestoreIp)
            disposable.add(subscribePersistedIp())
        disposable.add(subscribeIpValidation())
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}