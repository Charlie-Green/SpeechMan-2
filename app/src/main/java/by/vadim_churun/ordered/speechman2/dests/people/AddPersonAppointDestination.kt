package by.vadim_churun.ordered.speechman2.dests.people

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.adapters.PersonPotentialAppointsAdapter
import by.vadim_churun.ordered.speechman2.db.entities.Person
import by.vadim_churun.ordered.speechman2.db.objs.SeminarHeader
import by.vadim_churun.ordered.speechman2.model.filters.SeminarsFilter
import by.vadim_churun.ordered.speechman2.model.objects.DecodedImage
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.add_person_appoint_destination.*


class AddPersonAppointDestination: SpeechManFragment(R.layout.add_person_appoint_destination)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT

    companion object
    {
        val KEY_PERSON_ID = "personID"
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var person: Person? = null
    private var lastSearchQuery = ""

    private fun setEmptyText()
    {
        if(lastSearchQuery.isEmpty()) {
            tvEmpty.text = super.getString(
                R.string.fs_all_seminars_appointed,
                person?.name ?: ""
            )
        } else {
            tvEmpty.setText(R.string.msg_no_seminars_found)
        }
    }

    private fun applyPerson()
    {
        tvPersonName.text = person?.name ?: ""
        setEmptyText()
    }

    private fun setAdapter(headers: List<SeminarHeader>, personID: Int)
    {
        val metrs = DisplayMetrics()
        super.requireActivity().windowManager.defaultDisplay.getMetrics(metrs)
        val widthDP = (metrs.widthPixels / metrs.density).toInt()
        recvSeminars.layoutManager = recvSeminars.layoutManager
            ?: GridLayoutManager(super.requireContext(), widthDP / 240)
        val newAdapter = PersonPotentialAppointsAdapter(
            super.requireContext(), headers, personID, super.requireFragmentManager() )
        recvSeminars.swapAdapter(newAdapter, true)

        tvEmpty.isVisible = headers.isEmpty()
        prbSeminarsLoad.visibility = View.GONE
    }

    private fun applyImage(image: DecodedImage)
    {
        val adapter = recvSeminars.adapter as PersonPotentialAppointsAdapter
        adapter.setAvatar(image)
    }

    private fun setupSearch()
    {
        super.setupSearchViewLayoutBehaviour(vSearch, tvPersonName, tvAddAppointLabel)
        vSearch.setOnQueryTextListener( object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean
                = true

            override fun onQueryTextChange(newText: String): Boolean
            {
                lastSearchQuery = newText
                SpeechManAction.SetSeminarsFilter( SeminarsFilter(newText, true) ).also {
                    this@AddPersonAppointDestination.viewModel
                        .actionSubject
                        .onNext(it)
                }

                setEmptyText()
                return true
            }
        } )
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()
    private var decodeID: Int? = null

    private fun subscribePerson(personID: Int)
        = super.viewModel.createPersonObservable(personID)
            .doOnNext { p ->
                person = p
                applyPerson()
            }.subscribe()

    private fun subscribeDecodedImages()
        = super.viewModel.createDecodedImagesObservable()
            .doOnNext { image ->
                if(image.requestID == decodeID)
                    applyImage(image)
            }.subscribe()

    private fun subscribeHeaders(personID: Int)
        = SpeechManAction.SetSeminarsFilter( SeminarsFilter(lastSearchQuery, true) ).also {
            // First, set filter to forbid logically deleted seminars.
            super.viewModel.actionSubject.onNext(it)
        }.let {
            // Then, subscribe.
            super.viewModel.createSemHeadersNotForPersonObservable(personID)
                .doOnNext { headers ->
                    setAdapter(headers, personID)
                    decodeID = super.viewModel.nextImageDecodeID
                    super.viewModel.actionSubject
                        .onNext( SpeechManAction.DecodeImages(decodeID!!, headers) )
                }.subscribe()
        }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    { setupSearch() }

    override fun onStart()
    {
        super.onStart()
        val personID = super.getIntArgument(KEY_PERSON_ID, null, "KEY_PERSON_ID")
        disposable.add(subscribeDecodedImages())
        disposable.add(subscribeHeaders(personID))
        disposable.add(subscribePerson(personID))
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}