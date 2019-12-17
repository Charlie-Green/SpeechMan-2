package by.vadim_churun.ordered.speechman2.dests.sems

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.adapters.SeminarsAdapter
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.model.exceptions.ImageNotDecodedException
import by.vadim_churun.ordered.speechman2.model.filters.SeminarsFilter
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.seminars_list_destination.*


class SeminarsListDestination: SpeechManFragment(R.layout.seminars_list_destination)
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION:

    private fun navigateAddSeminar()
        = findNavController().navigate(R.id.actAddSeminar)


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var decodeID: Int? = null
    private var headers: List<SeminarHeaderX>? = null

    private fun setAdapter()
    {
        val mHeaders = headers ?: return
        etCount.text = super.getResources()
            .getString(R.string.fs_seminars_count, mHeaders.size)
        recvSems.layoutManager = recvSems.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = SeminarsAdapter(
            super.requireContext(), mHeaders, findNavController(), super.requireFragmentManager() )
        recvSems.swapAdapter(newAdapter, true)
    }

    private fun requestAvatars(source: List<SeminarHeaderX>)
    {
        decodeID = super.viewModel.nextImageDecodeID
        SpeechManAction.DecodeImages(decodeID!!, source).also {
            super.viewModel.actionSubject.onNext(it)
        }
    }

    private fun applyAvatar(avatar: DecodedImage)
    {
        if(avatar.requestID == decodeID)
            (recvSems.adapter as? SeminarsAdapter)?.setAvatar(avatar)
    }

    private fun setupSearch()
    {
        super.setupSearchViewLayoutBehaviour(vSearch, tvDestTitle)
        vSearch.setOnQueryTextListener( object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean
                = true    // The request has been handled.

            override fun onQueryTextChange(newText: String): Boolean
            {
                SeminarsFilter(newText, true).let {
                    SpeechManAction.SetSeminarsFilter(it)
                }.also {
                    this@SeminarsListDestination.viewModel.actionSubject.onNext(it)
                }
                return true    // The request was handled.
            }
        } )
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeSeminars()
        = super.viewModel.createSeminarHeadersObservable()
            .doOnNext { mHeaders ->
                headers = mHeaders
                setAdapter()
                requestAvatars(mHeaders)
            }.subscribe()

    private fun subscribeAvatars()
        = super.viewModel.createDecodedImagesObservable()
            .onErrorResumeNext { thr: Throwable ->
                val mHeaders = headers
                if(thr is ImageNotDecodedException &&
                    thr.requestID == decodeID &&
                    mHeaders != null ) {
                    super.viewModel.createSeminarObservable(mHeaders[thr.listPosition].base.ID)
                        .doOnNext { s ->
                            val goodSem = Seminar(s.ID, s.name, s.city, s.address,
                                s.content, null, s.costing, s.isLogicallyDeleted )
                            super.viewModel.actionSubject
                                .onNext( SpeechManAction.UpdateSeminar(goodSem) )
                        }.subscribe()
                        .also { disposable.add(it) }
                }
                Observable.empty()
            }.doOnNext { avatar ->
                applyAvatar(avatar)
            }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        setupSearch()
        fabAddSem.setOnClickListener { navigateAddSeminar() }
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(subscribeSeminars())
        disposable.add(subscribeAvatars())
    }

    override fun onStop()
    {
        decodeID?.also { super.viewModel.cancelImageDecodeRequest(it) }
        disposable.clear()
        super.onStop()
    }
}