package by.vadim_churun.ordered.speechman2.dialogs.sems

import android.os.Bundle
import android.view.View
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.db.objs.SeminarHeader
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.seminar_delete_dialog.*
import java.text.SimpleDateFormat


class SeminarDeleteDialog: SpeechManFragment(R.layout.seminar_delete_dialog)
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        val KEY_SEMINAR_ID = "semID"

        // TODO: Localize.
        private val START_FORMAT = SimpleDateFormat("dd.MM.yyyy, HH:mm")
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var actualHeader: SeminarHeader? = null

    private fun applyHeader()
    {
        val header = actualHeader
        buDelete.isEnabled = (header != null)
        header ?: return

        tvName.text = header.name
        val start = header.start?.time
        if(start == null)
            tvDate.setText(R.string.msg_unknown_date)
        else
            tvDate.text = super.requireContext()
                .getString(R.string.fs_seminar_start, START_FORMAT.format(start))
    }

    private fun deleteSeminar()
    {
        val id = actualHeader?.ID ?: return
        super.viewModel.actionSubject
            .onNext( SpeechManAction.DeleteSeminar(id) )
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeHeader(seminarID: Int)
        = super.viewModel.createSeminarHeaderObservable(seminarID)
            .doOnNext { header ->
                this.actualHeader = header
                applyHeader()
            }.subscribe()


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        applyHeader()
        buCancel.setOnClickListener { super.dismiss() }
        buDelete.setOnClickListener { deleteSeminar(); super.dismiss() }
    }

    override fun onStart()
    {
        super.onStart()
        val semID = super.getIntArgument(KEY_SEMINAR_ID, null, "KEY_SEMINAR_ID")
        disposable.add(subscribeHeader(semID))
    }

    override fun onStop()
    {
        super.onStop()
        disposable.clear()
    }
}