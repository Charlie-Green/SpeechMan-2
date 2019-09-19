package by.vadim_churun.ordered.speechman2.dialogs.sems

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.model.objects.SeminarAppointsBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.remove_participants_dialog.*
import kotlinx.android.synthetic.main.remove_participants_dialog.tvCount


class RemoveParticipantsDialog: SpeechManFragment(R.layout.remove_participants_dialog)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var builder: SeminarAppointsBuilder? = null

    private fun applyBuilder()
    {
        tvCount.text = builder?.removedAppoints?.size?.toString() ?: ""
    }

    private fun removeParticipants()
    {
        builder?.let {
            SpeechManAction.SaveSeminarAppoints(it)
        }?.also {
            super.viewModel.actionSubject.onNext(it)
            findNavController().navigateUp()
            super.dismiss()
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeBuilder()
        = super.viewModel.createSemAppointsBuilderObservable()
            .doOnNext { mBuilder ->
                builder = mBuilder
                applyBuilder()
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        buDelete.setOnClickListener { removeParticipants() }
        buCancel.setOnClickListener { super.dismiss() }
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