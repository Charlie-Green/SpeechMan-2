package by.vadim_churun.ordered.speechman2.dialogs.sems

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.semcosts_participants_dialog.*


class SemCostParticipantsDialog:
    SpeechManFragment(R.layout.semcosts_participants_dialog)
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        private val LOGTAG = SemCostParticipantsDialog::class.java.simpleName
        const val KEY_COST_POSITION = "costPosition"
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var fullBuilder: SeminarBuilder? = null
    private var costBuilder: SeminarBuilder.CostBuilder? = null

    private fun applyBuilder()
    {
        buSave.isEnabled = (costBuilder != null)
        prbBuilderLoad.isVisible = (costBuilder == null)
        etCount.setText(costBuilder?.minParticipants?.toString() ?: "")
    }

    private fun applyInput()
    {
        val mFullBuilder = fullBuilder ?: return
        val mCostBuilder = costBuilder ?: return
        SpeechManAction.ApplySemCostParticipants(
            etCount.text.toString(),
            mCostBuilder,
            mFullBuilder
        ).also {
            super.viewModel.actionSubject.onNext(it)
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeBuilder(costPosition: Int)
        = super.viewModel.createSeminarBuilderObservable()
            .doOnNext { semBuilder ->
                fullBuilder = semBuilder
                costBuilder = semBuilder.costBuilders[costPosition]
                applyBuilder()
            }.subscribe()


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        applyBuilder()
        buCancel.setOnClickListener { super.dismiss() }
        buSave.setOnClickListener { applyInput(); super.dismiss() }
    }

    override fun onStart()
    {
        super.onStart()
        val costPosition = super.getIntArgument(KEY_COST_POSITION, null, "KEY_COST_POSITION")
        disposable.add(subscribeBuilder(costPosition))
    }

    override fun onStop()
    {
        super.onStop()
        disposable.clear()
    }
}