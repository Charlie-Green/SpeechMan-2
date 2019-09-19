package by.vadim_churun.ordered.speechman2.dialogs.people

import android.os.Bundle
import android.view.View
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.db.objs.AppointedSeminar
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.delete_person_apoint_dialog.*


class DeletePersonAppointDialog: SpeechManFragment(R.layout.delete_person_apoint_dialog)
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        const val KEY_PERSON_ID = "personID"
        const val KEY_SEMINAR_ID = "seminarID"
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var appsem: AppointedSeminar? = null

    private fun applyAppSem()
    {
        tvName.text = appsem?.seminar?.name ?: ""
    }

    private fun deleteAppoint()
    {
        val mAppSem = appsem ?: return
        super.viewModel.actionSubject
            .onNext( SpeechManAction.DeleteAppointment(mAppSem.appoint) )
        super.dismiss()
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeAppSem(personID: Int, seminarID: Int)
        = super.viewModel.createAppointedSeminarObservable(personID, seminarID)
            .doOnNext { mAppSem ->
                appsem = mAppSem
                applyAppSem()
            }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        buDelete.setOnClickListener { deleteAppoint() }
        buCancel.setOnClickListener { super.dismiss() }
    }

    override fun onStart()
    {
        super.onStart()
        val personID = super.getIntArgument(KEY_PERSON_ID, null, "KEY_PERSON_ID")
        val seminarID = super.getIntArgument(KEY_SEMINAR_ID, null, "KEY_SEMINAR_ID")
        disposable.add(subscribeAppSem(personID, seminarID))
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}