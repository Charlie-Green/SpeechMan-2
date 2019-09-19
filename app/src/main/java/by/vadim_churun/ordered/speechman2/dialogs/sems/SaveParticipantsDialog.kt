package by.vadim_churun.ordered.speechman2.dialogs.sems

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.*
import by.vadim_churun.ordered.speechman2.adapters.SaveParticipantsAdapter
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.model.objects.SeminarAppointsBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.save_participants_dialog.*


class SaveParticipantsDialog: SpeechManFragment(R.layout.save_participants_dialog)
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var builder: SeminarAppointsBuilder? = null

    private fun applyBuilder()
    {
        val mBuilder = builder ?: return
        recvAddedParticips.layoutManager = recvAddedParticips.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = SaveParticipantsAdapter(
            super.requireContext(), mBuilder, super.viewModel.actionSubject )
        recvAddedParticips.swapAdapter(newAdapter, true)

        if(mBuilder.removedAppoints.isEmpty()) {
            tvRemovedCount.visibility = View.GONE
        } else {
            tvRemovedCount.visibility = View.VISIBLE
            tvRemovedCount.text = super.getString(
                R.string.fs_removed_participants_count, mBuilder.removedAppoints.size )
        }

        prbBuilderLoad.visibility = View.GONE
    }

    private fun applySeminar(seminar: Seminar)
    { tvSeminarName.text = seminar.name }

    private fun saveChanges()
    {
        val adapter = recvAddedParticips.adapter as? SaveParticipantsAdapter ?: return
        fun getAddedPersonName(position: Int): String
        {
            val personID = adapter.builder.addedAppoints[position].personID

            // TODO: Optimize this search.
            return adapter.builder.allPeople.find {
                it.ID == personID
            }!!.name
        }

        val errorPositions = adapter.commitCosts()
        when(errorPositions.size)
        {
            0 -> {
                SpeechManAction.SaveSeminarAppoints(adapter.builder).also {
                    super.viewModel.actionSubject.onNext(it)
                }
                super.dismiss()
                findNavController().navigateUp()
            }

            1 -> {
                tvError.visibility = View.VISIBLE
                tvError.text = super.getString(
                    R.string.fs_incorrect_appoint_cost, getAddedPersonName(errorPositions[0]) )
                recvAddedParticips.scrollToPosition(errorPositions[0])
            }

            else -> {
                tvError.visibility = View.VISIBLE
                tvError.text = super.getString(
                    R.string.fs_incorrect_appoint_cost_expanded,
                    getAddedPersonName(errorPositions[0]),
                    errorPositions.size - 1
                )
                recvAddedParticips.scrollToPosition(errorPositions[0])
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeSeminar(seminarID: Int)
        = super.viewModel.createSeminarObservable(seminarID)
            .doOnNext { seminar ->
                applySeminar(seminar)
            }.subscribe()

    private fun subscribeBuilder()
        = super.viewModel.createSemAppointsBuilderObservable()
            .doOnNext { mBuilder ->
                builder = mBuilder
                applyBuilder()
                disposable.add(subscribeSeminar(mBuilder.seminarID))
            }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // GESTURES:

    private val cancelGestListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean
            = true

        override fun onDoubleTap(e: MotionEvent?): Boolean
            = false.also { this@SaveParticipantsDialog.dismiss() }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        buSave.setOnClickListener { saveChanges() }

        val cancelGestDetector = GestureDetector(super.requireContext(), cancelGestListener)
        buCancel.setOnTouchListener { v, event ->
            cancelGestDetector.onTouchEvent(event)
        }
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(subscribeBuilder())
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}