package by.vadim_churun.ordered.speechman2.dests.people

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.adapters.AppointedSeminarsAdapter
import by.vadim_churun.ordered.speechman2.db.entities.Person
import by.vadim_churun.ordered.speechman2.db.objs.AppointedSeminar
import by.vadim_churun.ordered.speechman2.model.filters.SeminarsFilter
import by.vadim_churun.ordered.speechman2.model.objects.DecodedImage
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.person_appoints_list_destination.*


class PersonAppointsListDestination:
    SpeechManFragment(R.layout.person_appoints_list_destination)
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        const val KEY_PERSON_ID = "personID"
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var person: Person? = null
    private var appsems: List<AppointedSeminar>? = null

    private fun applyPerson()
    { tvPersonName.text = person?.name ?: "" }

    private fun setAdapter()
    {
        val mAppSems = appsems ?: return

        tvCount.text = super.getString(R.string.fs_person_appointments_count, mAppSems.size)
        recvAppoints.layoutManager = recvAppoints.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = AppointedSeminarsAdapter(
            super.requireContext(), mAppSems, super.requireFragmentManager() )
        recvAppoints.swapAdapter(newAdapter, true)

        prbAppointsLoad.visibility = View.GONE
    }

    private fun applyDecodedImage(image: DecodedImage)
    {
        val adapter = recvAppoints.adapter as AppointedSeminarsAdapter
        adapter.setImage(image)
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETTING UP LISTENERS:

    private var lastSearchQuery = ""

    private fun setupSearch()
    {
        super.setupSearchViewLayoutBehaviour(vSearch, tvPersonName)
        vSearch.setOnQueryTextListener( object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean
                = true

            override fun onQueryTextChange(newText: String): Boolean
            {
                lastSearchQuery = newText
                setFilter()
                return true
            }
        } )
    }

    private fun navigateAddAppoint()
    {
        val personID = person?.ID ?: return
        Bundle().apply {
            putInt(AddPersonAppointDestination.KEY_PERSON_ID, personID)
        }.also {
            findNavController().navigate(R.id.actAddPersonAppoint, it)
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()
    private var decodeID: Int? = null

    private fun setFilter()
    {
        SeminarsFilter(lastSearchQuery, true).let {
            SpeechManAction.SetAppointedSeminarsFilter(it)
        }.also {
            super.viewModel.actionSubject.onNext(it)
        }
    }

    private fun subscribePerson(personID: Int)
        = super.viewModel.createPersonHeaderObservable(personID)
            .doOnNext { header ->
                person = header.person
                applyPerson()
            }.subscribe()

    private fun subscribeDecodedImages()
        = super.viewModel.createDecodedImagesObservable()
            .onErrorResumeNext { thr: Throwable ->
                appsems?.also {
                    super.handleImageNotDecoded(thr, decodeID!!, it) { appsem -> appsem.seminar }
                }
                Observable.empty()
            }.doOnNext { image ->
                if(image.requestID == decodeID)
                    applyDecodedImage(image)
            }.subscribe()

    private fun subscribeAppSems(personID: Int)
        = super.viewModel.createAppointedSeminarsObservable(personID)
            .doOnNext { mAppSems ->
                appsems = mAppSems
                setAdapter()

                decodeID?.also { super.viewModel.cancelImageDecodeRequest(it) }
                decodeID = super.viewModel.nextImageDecodeID
                SpeechManAction.DecodeImages(decodeID!!, mAppSems).also {
                    super.viewModel.actionSubject.onNext(it)
                }
            }.subscribe()

    private fun cancelImageDecode()
    {
        decodeID?.also { super.viewModel.cancelImageDecodeRequest(it) }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        setupSearch()
        savedInstanceState ?: setFilter()  // Set initial filter to forbid logically deleted Appointments.
        fabAdd.setOnClickListener { navigateAddAppoint() }
    }

    override fun onStart()
    {
        super.onStart()
        val personID = super.getIntArgument(KEY_PERSON_ID, null, "KEY_PERSON_ID")
        disposable.add(subscribeDecodedImages())
        disposable.add(subscribeAppSems(personID))
        disposable.add(subscribePerson(personID))
    }

    override fun onStop()
    {
        cancelImageDecode()
        disposable.clear()
        super.onStop()
    }
}