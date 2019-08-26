package by.vadim_churun.ordered.speechman2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.repo.PeopleRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class SpeechManViewModel(app: Application): AndroidViewModel(app)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // WRAPPING PEOPLE REPOSITORY:

    private val peopleRepo = PeopleRepository(super.getApplication())

    fun createPeopleObservable()
        = peopleRepo.createPeopleObservable()

    fun createPersonInfoObservable()
        = peopleRepo.createInfoObservable()

    fun validatePersonName(name: CharSequence)
        = peopleRepo.validateName(name)


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // REACTIVE:

    private val disposable = CompositeDisposable()
    val actionSubject = PublishSubject.create<SpeechManAction>()

    private fun observeActionSubject()
        = actionSubject.observeOn(Schedulers.single())
            .doOnNext { action ->
                when(action)
                {
                    is SpeechManAction.AddPerson -> {
                        val msgAddingPerson = super
                            .getApplication<Application>().getString(R.string.msg_adding_person)
                        actionSubject.onNext( SpeechManAction.ShowMessage(false, msgAddingPerson) )
                        peopleRepo.addOrUpdate(action.person)
                    }

                    is SpeechManAction.RequestPersonInfos -> {
                        peopleRepo.infoRequestSubject.onNext(action.people)
                    }

                    is SpeechManAction.SetPeopleFilter -> {
                        peopleRepo.filterSubject.onNext(action.filter)
                    }
                }

            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEWMODEL LIFECYCLE:

    init
    {
        disposable.add(observeActionSubject())
    }

    override fun onCleared()
        = disposable.clear()
}