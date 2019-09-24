package by.vadim_churun.ordered.speechman2.dialogs.associations

import android.view.View
import android.widget.*
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.*
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.appointment_dialog.*


/** Common abstraction for any [Dialog] allowing creation/updating of an [Appointment]. **/
abstract class AppointmentDialog: SpeechManFragment(R.layout.appointment_dialog)
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        const val KEY_PERSON_ID = "personID"
        const val KEY_SEMINAR_ID = "semID"
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP:

    private var pers: Person? = null
    private var sem: Seminar? = null

    protected val cancelButton: Button
        get() = buCancel

    protected val saveButton: Button
        get() = buSave

    protected var person: Person?
        get() = pers
        set(value) {
            pers = value
            tvPersonName.text = value?.name ?: ""
        }

    protected var seminar: Seminar?
        get() = sem
        set(value) {
            sem = value
            tvSeminarName.text = value?.name ?: ""
        }

    protected fun applyAppointment(appoint: Appointment)
    {
        etPurchase.setText("${appoint.purchase}")
        etCost.setText("${appoint.cost}")
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // SAVING:

    protected fun saveAppointment(historyStatus: HistoryStatus)
    {
        fun createMoney(et: EditText, errorMessageResId: Int): Money?
        {
            try {
                return Money.parse(et.text.toString())
            } catch(exc: Exception) {
                tvError.visibility = View.VISIBLE
                tvError.setText(errorMessageResId)
                return null
            }
        }

        val personID = person?.ID ?: return; val seminarID = seminar?.ID ?: return
        val purchase = createMoney(etPurchase, R.string.msg_appointment_purchase_incorrect) ?: return
        val cost = createMoney(etCost, R.string.msg_appointment_cost_incorrect) ?: return
        Appointment(personID, seminarID, purchase, cost, historyStatus, false).let {
            SpeechManAction.SaveAppointment(it)
        }.also {
            super.viewModel.actionSubject.onNext(it)
        }

        super.dismiss()
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    protected fun subscribePerson(): Disposable
    {
        val personID = super.getIntArgument(KEY_PERSON_ID, null, "KEY_PERSON_ID")
        return super.viewModel.createPersonObservable(personID)
            .doOnNext { p ->
                this.person = p
            }.subscribe()
    }

    protected fun subscribeSeminar(): Disposable
    {
        val seminarID = super.getIntArgument(KEY_SEMINAR_ID, null, "KEY_SEMINAR_ID")
        return super.viewModel.createSeminarObservable(seminarID)
            .doOnNext { s ->
                this.seminar = s
            }.subscribe()
    }
}