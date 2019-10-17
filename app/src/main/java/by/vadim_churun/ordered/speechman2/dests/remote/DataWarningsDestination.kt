package by.vadim_churun.ordered.speechman2.dests.remote

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.data_warnings_destination.*


class DataWarningsDestination: SpeechManFragment(R.layout.data_warnings_destination)
{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var lastRemoteData: RemoteData? = null

    private fun setAdapter()
    {
        val warns = lastRemoteData!!.warnings
        tvWarningsCount.text = super.requireContext()
            .getString(R.string.fs_data_warnings_count, warns.size)
        recvWarnings.layoutManager = recvWarnings.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = TODO()
        recvWarnings.swapAdapter(newAdapter, true)
    }

    private fun doCheckAll()
    {
        // TODO
    }

    private fun doUncheckAll()
    {
        // TODO
    }


    private fun navigateLacks()
    { findNavController().navigate(R.id.actWarningsToLacks) }

    private fun finishImport()
    {
        SpeechManAction.ShowMessage(false,
            super.getResources(),
            R.string.msg_import_finished
        ).also {
            super.viewModel
                .actionSubject
                .onNext(it)
        }

        findNavController().navigate(R.id.actToRemote)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeRemoteData()
        = super.viewModel
            .createRemoteDataObservable()
            .mergeWith(super.viewModel.getKeptRemoteDataRx())
            .doOnNext { rd ->
                lastRemoteData = rd
                if(rd.warnings.isNotEmpty())
                    setAdapter()
                else if(rd.lacks.isNotEmpty())
                    navigateLacks()
                else
                    finishImport()
            }.subscribe()

    private fun saveRemoteData()
    {
        val rd = lastRemoteData ?: return
        super.viewModel
            .actionSubject
            .onNext( SpeechManAction.SaveRemoteData(rd) )
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        optionsPanel.setOnNavigationItemReselectedListener { mi ->
            when(mi.itemId)
            {
                R.id.miCheckAll   -> doCheckAll()
                R.id.miUncheckAll -> doUncheckAll()
                R.id.miSave       -> saveRemoteData()
            }
        }
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(subscribeRemoteData())
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}