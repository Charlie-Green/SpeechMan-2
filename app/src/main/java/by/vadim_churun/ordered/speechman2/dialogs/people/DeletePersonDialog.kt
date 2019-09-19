package by.vadim_churun.ordered.speechman2.dialogs.people

import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.delete_person_dialog.*


class DeletePersonDialog: SpeechManFragment(R.layout.delete_person_dialog)
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        const val KEY_PERSON_ID = "personID"
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private lateinit var person: Person

    private fun applyPerson(pers: Person)
    {
        this.person = pers
        buDelete.isEnabled = true
        tvName.text = pers.name
    }

    private fun applyType(typ: PersonType)
    { tvType.text = typ.label }

    private fun deletePerson()
        = super.viewModel.actionSubject
            .onNext( SpeechManAction.DeletePerson(person) )


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribePerson(personID: Int)
        = super.viewModel.createPersonObservable(personID)
            .doOnNext { pers ->
                applyPerson(pers)
            }.subscribe()

    private fun subscribeType()
    {
        // TODO
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onStart()
    {
        super.onStart()
        buDelete.isEnabled = false
        val personID = super.getIntArgument(KEY_PERSON_ID, null, "KEY_PERSON_ID")
        disposable.add(subscribePerson(personID))
        buCancel.setOnClickListener { super.dismiss() }
        buDelete.setOnClickListener { deletePerson(); super.dismiss() }
    }

    override fun onStop()
    {
        super.onStop()
        disposable.clear()
    }
}