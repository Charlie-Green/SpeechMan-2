package by.vadim_churun.ordered.speechman2.dialogs.associations

import android.os.Bundle
import android.view.View
import by.vadim_churun.ordered.speechman2.db.objs.HistoryStatus
import io.reactivex.disposables.CompositeDisposable


class AddAppointmentDialog: AppointmentDialog()
{
    private val disposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.cancelButton.setOnClickListener { super.dismiss() }
        super.saveButton.setOnClickListener {
            super.saveAppointment(HistoryStatus.USUAL)
        }
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(super.subscribePerson())
        disposable.add(super.subscribeSeminar())
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}