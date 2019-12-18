package by.vadim_churun.ordered.speechman2.dests.people

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.model.objects.PersonHeader
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.person_detail_destination.*


class PersonDetailDestination:
    SpeechManFragment(R.layout.person_detail_destination)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        private const val KEY_EDITED_NAME = "editName"
        const val KEY_PERSON_ID = "personID"
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION:

    private fun lookupAppoints()
    {
        val head = header ?: return
        Bundle().apply {
            putInt(PersonAppointsListDestination.KEY_PERSON_ID, head.person.ID!!)
        }.also {
            findNavController().navigate(R.id.actLookupPersonAppointments, it)
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var header: PersonHeader? = null
    private var editedName: String? = null

    private fun applyPerson(head: PersonHeader)
    {
        this.header = head
        tvAppointsCount.text = "${head.countAppoints}"
        tvOrdersCount.text = "${head.countOrders}"
        etName.setText(editedName ?: head.person.name)
        prbPersonLoad.visibility = /*if(type == null) View.VISIBLE else*/ View.GONE
    }


    private fun updatePerson()
    {
        prbPersonLoad.visibility = View.VISIBLE

        val mType: Int? = /* TODO */ null
        Person(header!!.person.ID, etName.text.toString(), mType).let {
            SpeechManAction.UpdatePerson(it)
        }.also {
            super.viewModel.actionSubject.onNext(it)
        }

    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // GESTURES:

    private fun setupEditUI()
    {
        fun setNameViewsProperties(isEditEnabled: Boolean)
        {
            val visib = if(isEditEnabled) View.VISIBLE else View.GONE
            imgvSaveEditName.visibility = visib
            imgvCancelEditName.visibility = visib
            etName.isEnabled = isEditEnabled
            imgvCancelEditName.isEnabled = isEditEnabled
            imgvSaveEditName.isEnabled = isEditEnabled
        }

        setNameViewsProperties(editedName != null)
        val nameDTapListener = object: GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true

            override fun onDoubleTap(event: MotionEvent): Boolean {
                setNameViewsProperties(header != null)
                return false
            }
        }
        val nameDTapDetector = GestureDetector(super.requireContext(), nameDTapListener)
        etName.setOnTouchListener { _, event -> nameDTapDetector.onTouchEvent(event) }

        imgvCancelEditName.setOnClickListener {
            editedName = null
            header?.also { applyPerson(it) }
            setNameViewsProperties(false)
        }
        imgvSaveEditName.setOnClickListener {
            editedName = null
            updatePerson()
            setNameViewsProperties(false)
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribePerson(personID: Int)
        = super.viewModel.createPersonHeaderObservable(personID)
            .doOnNext { head ->
                applyPerson(head)
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        editedName = savedInstanceState?.getString(KEY_EDITED_NAME)
        fabLookAppoints.setOnClickListener { lookupAppoints() }
        setupEditUI()
    }

    override fun onStart()
    {
        super.onStart()
        val personID = super.getIntArgument(KEY_PERSON_ID, null, "KEY_PERSON_ID")
        disposable.add(subscribePerson(personID))
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        if(etName?.isEnabled == true)
            outState.putString(KEY_EDITED_NAME, etName.text.toString())
    }

    override fun onStop()
    {
        super.onStop()
        disposable.clear()
    }
}