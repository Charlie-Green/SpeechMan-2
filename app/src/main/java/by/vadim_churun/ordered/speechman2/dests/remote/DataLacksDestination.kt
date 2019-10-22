package by.vadim_churun.ordered.speechman2.dests.remote

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.adapters.DataLacksAdapter
import by.vadim_churun.ordered.speechman2.model.lack_info.*
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.data_lacks_destination.*


class DataLacksDestination: SpeechManFragment(R.layout.data_lacks_destination)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var lastRemoteData: RemoteData? = null
    private var isSaveUserRequested = false

    private fun applyLacks()
    {
        val lacks = lastRemoteData!!.lacks
        tvLacksCount.text = super.requireContext()
            .getString(R.string.fs_data_lacks_count, lacks.size)
        recvLacks.layoutManager = recvLacks.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = DataLacksAdapter(
            super.requireContext(), super.requireFragmentManager(), lacks )
        recvLacks.swapAdapter(newAdapter, true)
    }

    private fun applyLackInfos(infos: List<DataLackInfo?>) {
        val adapter = recvLacks.adapter as DataLacksAdapter
        adapter.setLackInfos(infos)
    }

    private fun navigateWarnings()
    {
        val rd = lastRemoteData ?: return

        disposable.clear()
        super.viewModel.keepRemoteData(rd)
        findNavController().navigate(R.id.actLacksToWarnings)
    }

    private fun finishImport()
    {
        val msg = super.getResources().getString(R.string.msg_import_finished)
        super.viewModel.actionSubject.apply {
            onNext( SpeechManAction.SetBackButtonLock(false) )
            onNext( SpeechManAction.ShowMessage(false, msg) )
        }
        findNavController().navigate(R.id.actToRemote)
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeRemoteData()
        = super.viewModel
            .createRemoteDataObservable()
            .mergeWith(super.viewModel.getKeptRemoteDataRx())
            .doOnNext { rd ->
                if(isSaveUserRequested) {
                    // Notify the user that their changes are applied:
                    SpeechManAction.ShowMessage(false,
                        super.getResources(),
                        R.string.msg_changes_applied
                    ).also {
                        super.viewModel
                            .actionSubject
                            .onNext(it)
                    }

                    isSaveUserRequested = false
                }

                // Proceed with the import process or finish it:
                lastRemoteData = rd
                if(rd.lacks.isEmpty()) {
                    if(rd.warnings.isEmpty())
                        finishImport()
                    else
                        navigateWarnings()
                } else {
                    applyLacks()

                    // Request DataLackInfo's for the updated RemoteData.lacks:
                    DataLackInfosRequest(rd.requestID, rd.entities, rd.lacks, rd.warnings).let {
                        SpeechManAction.RequestDataLackInfos(it)
                    }.also {
                        super.viewModel
                            .actionSubject
                            .onNext(it)
                    }
                }
            }.subscribe()

    private fun subscribeLackInfos()
        = super.viewModel
            .createLackInfosObservable()
            .filter { response ->
                response.requestID == lastRemoteData?.requestID
            }.doOnNext { response ->
                applyLackInfos(response.infos)
                prbLoad.visibility = View.GONE
            }.subscribe()

    private fun applyChanges()
    {
        val rd = lastRemoteData ?: return
        if(isSaveUserRequested) {
            super.viewModel
                .actionSubject
                .onNext( SpeechManAction.SaveRemoteData(rd) )
        } else {
            super.viewModel.keepRemoteData(rd)
        }
    }

    private fun lockBackButton()
    {
        super.viewModel
            .actionSubject
            .onNext( SpeechManAction.SetBackButtonLock(true) )
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fabSave.setOnClickListener {
            isSaveUserRequested = true
            applyChanges()
        }
    }

    override fun onStart()
    {
        lockBackButton()
        super.onStart()
        disposable.add(subscribeRemoteData())
        disposable.add(subscribeLackInfos())
    }

    override fun onStop()
    {
        disposable.clear()
        isSaveUserRequested = false; applyChanges()
        super.onStop()
    }
}