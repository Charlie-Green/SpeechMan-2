package by.vadim_churun.ordered.speechman2.dests.sems

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.*
import by.vadim_churun.ordered.speechman2.adapters.EditParticipantsAdapter
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.dialogs.sems.*
import by.vadim_churun.ordered.speechman2.model.filters.PeopleFilter
import by.vadim_churun.ordered.speechman2.model.objects.SeminarAppointsBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.edit_participants_destination.*


class EditParticipantsDestination: SpeechManFragment(R.layout.edit_participants_destination)
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        const val KEY_SEMINAR_ID = "semID"
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var builder: SeminarAppointsBuilder? = null
    private var people: List<Person>? = null

    private fun applySeminar(seminar: Seminar)
    { tvSeminarName.text = seminar.name }

    private fun setAdapter()
    {
        val mBuilder = builder; val mPeople = people
        if(mBuilder == null || mPeople == null) {
            recvPeople.adapter = null
            prbBuilderLoad.visibility = View.VISIBLE
            return
        }

        recvPeople.layoutManager = recvPeople.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = EditParticipantsAdapter(super.requireContext(),
            mPeople, mBuilder, super.viewModel.actionSubject )
        recvPeople.swapAdapter(newAdapter, true)

        prbBuilderLoad.visibility = View.GONE
    }

    private fun updateParticipantsCount()
    {
        val mBuilder = builder
        if(mBuilder == null) {
            tvCount.text = ""
            tvAddCount.text = ""
            tvRemoveCount.text = ""
            return
        }
        tvCount.text = super.getString(R.string.fs_participants_expanded_count,
            mBuilder.initialAppointsCount,
            mBuilder.allPeople.size )
        tvAddCount.text = super.getString(
            R.string.fs_participants_to_add_count, mBuilder.addedAppoints.size )
        tvRemoveCount.text = super.getString(
            R.string.fs_participants_to_remove_count, mBuilder.removedAppoints.size )
    }

    private fun saveChanges()
    {
        val mBuilder = builder ?: return
        val needAdd = mBuilder.addedAppoints.isNotEmpty()
        val needRemove = mBuilder.removedAppoints.isNotEmpty()
        if(!needAdd && !needRemove) {
            SpeechManAction.ShowMessage(false,
                super.getResources(),
                R.string.msg_participants_not_changed
            ).also {
                super.viewModel.actionSubject.onNext(it)
            }
        } else if(needAdd) {
            SaveParticipantsDialog().apply {
                isCancelable = false
                show(super.requireFragmentManager(), null)
            }
        } else /*if(needRemove)*/ {
            RemoveParticipantsDialog().apply {
                isCancelable = false
                show(super.requireFragmentManager(), null)
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribePeople()
        = super.viewModel.createPeopleObservable()
            .doOnNext { mPeople ->
                people = mPeople
                setAdapter()
            }.subscribe()

    private fun subscribeSeminar(seminarID: Int)
        = super.viewModel.createSeminarObservable(seminarID)
            .doOnNext { seminar ->
                applySeminar(seminar)
            }.subscribe()

    private fun subscribeBuilder()
        = super.viewModel.createSemAppointsBuilderObservable()
            .doOnNext { mBuilder ->
                builder = mBuilder
                updateParticipantsCount()
                setAdapter()
                disposable.add(subscribeSeminar(mBuilder.seminarID))
            }.subscribe()

    private fun subscribeChangeAction()
        = super.viewModel.actionSubject
            .observeOn(AndroidSchedulers.mainThread())
            .filter { action ->
                action is SpeechManAction.ApplySemAppointsBuilderChange
            }.doOnNext { _ ->
                updateParticipantsCount()
            }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.setupSearchViewLayoutBehaviour(vSearch, tvSeminarName)
        vSearch.setOnQueryTextListener( object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean
                = true    // Query handled.

            override fun onQueryTextChange(newText: String): Boolean
            {
                PeopleFilter(newText, null).let {
                    SpeechManAction.SetPeopleFilter(it)
                }.also {
                    this@EditParticipantsDestination
                        .viewModel
                        .actionSubject
                        .onNext(it)
                }
                return true    // Query handled.
            }
        } )

        buSave.setOnClickListener { saveChanges() }
        if(savedInstanceState == null) {
            super.getIntArgument(KEY_SEMINAR_ID, null, "KEY_SEMINAR_ID").let {
                SpeechManAction.RetrieveSeminarAppointsBuilder(it)
            }.also {
                super.viewModel.actionSubject.onNext(it)
            }
        }
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(subscribeBuilder())
        disposable.add(subscribePeople())
        disposable.add(subscribeChangeAction())
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}