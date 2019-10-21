package by.vadim_churun.ordered.speechman2.dests.remote

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.adapters.DataWarningsAdapter
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData
import by.vadim_churun.ordered.speechman2.model.warning.DataWarning
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.data_warnings_destination.*


class DataWarningsDestination: SpeechManFragment(R.layout.data_warnings_destination)
{
    private val LOGTAG = "Import UI"

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var lastRemoteData: RemoteData? = null

    private fun setAdapter()
    {
        val warns = lastRemoteData!!.warnings
        tvWarningsCount.text = super.requireContext()
            .getString(R.string.fs_data_warnings_count, warns.size)
        recv.layoutManager = recv.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = DataWarningsAdapter(super.requireContext(), warns)
        recv.swapAdapter(newAdapter, true)
    }

    private fun setActionToAll(action: DataWarning.Action)
    {
        val adapter = recv.adapter as? DataWarningsAdapter ?: return
        prBar.visibility = View.VISIBLE
        adapter.setActionToAll(action)
        prBar.visibility = View.GONE
    }


    private fun navigateLacks()
    {
        val rd = lastRemoteData ?: return
        super.viewModel.keepRemoteData(rd)
        findNavController().navigate(R.id.actWarningsToLacks)
    }

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
                Log.i(LOGTAG, "Received ${rd.lacks.size} lacks and ${rd.warnings.size} warnings.")
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
        fabSave.setOnClickListener { saveRemoteData() }

        vMenu.menu.add(R.string.mi_update_all_warnings)
        vMenu.menu.add(R.string.mi_drop_all_warnings)
        vMenu.menu.add(R.string.mi_duplicate_all_warnings)
        vMenu.setOnMenuItemClickListener { mi ->
            val ctxt = super.requireContext()
            setActionToAll(
                when(mi.title)
                {
                    ctxt.getString(R.string.mi_update_all_warnings)
                        -> DataWarning.Action.UPDATE
                    ctxt.getString(R.string.mi_drop_all_warnings)
                        -> DataWarning.Action.DROP
                    ctxt.getString(R.string.mi_duplicate_all_warnings)
                        -> DataWarning.Action.DUPLICATE
                    else -> throw Exception("Unknown menu option \"${mi.title}\"")
                }
            )
            return@setOnMenuItemClickListener true
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