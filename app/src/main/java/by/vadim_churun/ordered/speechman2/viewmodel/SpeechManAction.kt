package by.vadim_churun.ordered.speechman2.viewmodel

import android.content.res.Resources
import by.vadim_churun.ordered.speechman2.db.entities.Person
import by.vadim_churun.ordered.speechman2.model.filters.PeopleFilter


sealed class SpeechManAction
{
    class AddPerson(val person: Person): SpeechManAction()
    class RequestPersonInfos(val people: List<Person>): SpeechManAction()
    class SetPeopleFilter(val filter: PeopleFilter): SpeechManAction()

    class ShowMessage: SpeechManAction
    {
        val message: String
        val showAsError: Boolean

        constructor(showAsError: Boolean, message: String)
        {
            this.message = message
            this.showAsError = showAsError
        }

        constructor(
            showAsError: Boolean, resources: Resources, messageResID: Int, vararg formatArgs: Any ):
            this(showAsError, resources.getString(messageResID, formatArgs))
    }
}