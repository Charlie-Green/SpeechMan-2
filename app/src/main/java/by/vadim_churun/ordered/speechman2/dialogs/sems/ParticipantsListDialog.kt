package by.vadim_churun.ordered.speechman2.dialogs.sems

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.adapters.ParticipantsAdapter
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.db.objs.Participant
import by.vadim_churun.ordered.speechman2.dests.sems.EditParticipantsDestination
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.participants_list_dialog.*


class ParticipantsListDialog:
SpeechManFragment(R.layout.participants_list_dialog)
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        const val KEY_SEMINAR_ID = "semID"
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var seminar: Seminar? = null

    private fun navigateEdit()
    {
        val sem = seminar ?: return
        Bundle().apply {
            putInt(EditParticipantsDestination.KEY_SEMINAR_ID, sem.ID!!)
        }.also {
            findNavController().navigate(R.id.actEditParticipants, it)
        }
    }

    private fun applySeminar()
    { tvSeminarName.text = seminar?.name ?: "" }

    private fun setAdapter(particips: List<Participant>)
    {
        tvCount.text = super.getString(R.string.fs_participants_count, particips.size)
        recvParticips.layoutManager = recvParticips.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = ParticipantsAdapter(
            super.requireContext(), particips, super.requireFragmentManager() )
        recvParticips.swapAdapter(newAdapter, true)
        val decor = DividerItemDecoration(super.requireContext(), DividerItemDecoration.VERTICAL)
        recvParticips.addItemDecoration(decor)

        prbParticipsLoad.visibility = View.GONE
    }

    private fun applyEditFABFocus(hasFocus: Boolean)
    { fabEdit.alpha = if(hasFocus) 1f else 0.4f }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeSeminar(seminarID: Int)
        = super.viewModel.createSeminarObservable(seminarID)
            .doOnNext { sem ->
                seminar = sem
                applySeminar()
            }.subscribe()

    private fun subscribeParticips(seminarID: Int)
        = super.viewModel.createParticipantsObservable(seminarID)
            .doOnNext { particips ->
                setAdapter(particips)
            }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        fabEdit.setOnClickListener { navigateEdit(); super.dismiss() }
        fabEdit.setOnFocusChangeListener { _, hasFocus -> applyEditFABFocus(hasFocus) }
    }

    override fun onStart()
    {
        super.onStart()
        val seminarID = super.getIntArgument(KEY_SEMINAR_ID, null, "KEY_SEMINAR_ID")
        disposable.add(subscribeSeminar(seminarID))
        disposable.add(subscribeParticips(seminarID))
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}