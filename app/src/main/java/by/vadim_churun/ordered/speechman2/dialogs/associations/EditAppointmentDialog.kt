package by.vadim_churun.ordered.speechman2.dialogs.associations

import android.os.Bundle
import android.view.View
import by.vadim_churun.ordered.speechman2.db.entities.Appointment
import io.reactivex.disposables.CompositeDisposable


class EditAppointmentDialog: AppointmentDialog()
{
    private val disposable = CompositeDisposable()
    private var appoint: Appointment? = null

    private fun subscribeAppSem(personID: Int, seminarID: Int)
        = super.viewModel.createAppointedSeminarObservable(personID, seminarID)
            .doOnNext { appsem ->
                appoint = appsem.appoint
                super.applyAppointment(appsem.appoint)
                super.seminar = appsem.seminar
            }.subscribe()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.cancelButton.setOnClickListener { super.dismiss() }
        super.saveButton.setOnClickListener {
            appoint?.also { super.saveAppointment(it.historyStatus) }
        }
    }

    override fun onStart()
    {
        super.onStart()
        val personID = super.getIntArgument(AppointmentDialog.KEY_PERSON_ID, null, "KEY_PERSON_ID")
        val seminarID = super.getIntArgument(AppointmentDialog.KEY_SEMINAR_ID, null, "KEY_SEMINAR_ID")
        disposable.add(super.subscribePerson())
        disposable.add(subscribeAppSem(personID, seminarID))
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}