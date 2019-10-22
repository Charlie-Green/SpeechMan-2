package by.vadim_churun.ordered.speechman2.dests.remote

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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
    private var lastRemoteData: RemoteData? = null

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private fun setMenuOverflowIcon()
    {
        val typval = TypedValue()
        super.requireContext().theme
            .resolveAttribute(android.R.attr.colorBackground, typval, true)
        val icon = vMenu.overflowIcon!!
        icon.setTint(typval.data)
        vMenu.overflowIcon = icon
    }

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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY AND NAVIGATION:

    private fun setActionToAll(action: DataWarning.Action)
    {
        val adapter = recv.adapter as? DataWarningsAdapter ?: return
        prBar.visibility = View.VISIBLE
        adapter.setActionToAll(action)
        prBar.visibility = View.GONE
    }

    private fun notifyChangesSaved()
    {
        val action = SpeechManAction.ShowMessage(
            false, super.getResources(), R.string.msg_changes_applied )
        super.viewModel.actionSubject.onNext(action)
    }

    private fun navigateLacks()
    {
        val rd = lastRemoteData ?: return
        super.viewModel.keepRemoteData(rd)
        findNavController().navigate(R.id.actWarningsToLacks)
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()
    private var isSaveUserRequested = false

    private fun subscribeRemoteData()
        = super.viewModel
            .createRemoteDataObservable()
            .mergeWith(super.viewModel.getKeptRemoteDataRx())
            .doOnNext { rd ->
                lastRemoteData = rd

                if(rd.warnings.isNotEmpty()) {
                    setAdapter()
                    if(isSaveUserRequested) {
                        notifyChangesSaved()
                        isSaveUserRequested = false
                    }
                } else if(rd.lacks.isNotEmpty()) {
                    navigateLacks()
                } else {
                    finishImport()
                }

                prBar.visibility = View.GONE
            }.subscribe()

    private fun saveRemoteData()
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        fabSave.setOnClickListener {
            isSaveUserRequested = true
            saveRemoteData()
        }

        setMenuOverflowIcon()
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
        lockBackButton()
        super.onStart()
        disposable.add(subscribeRemoteData())
    }

    override fun onStop()
    {
        disposable.clear()
        isSaveUserRequested = false; saveRemoteData()
        super.onStop()
    }
}