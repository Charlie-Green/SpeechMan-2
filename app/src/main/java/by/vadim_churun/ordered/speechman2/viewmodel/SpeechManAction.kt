package by.vadim_churun.ordered.speechman2.viewmodel

import android.content.res.Resources
import android.net.Network
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.SeminarHeader
import by.vadim_churun.ordered.speechman2.model.filters.*
import by.vadim_churun.ordered.speechman2.model.lack_info.DataLackInfosRequest
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.remote.lack.DataLack


sealed class SpeechManAction
{
    class AddPerson(val person: Person): SpeechManAction()
    class ApplyAvailableNetwork(val network: Network): SpeechManAction()
    class ApplySemAppointsBuilderChange: SpeechManAction()
    class ApplySemCostParticipants(val input: String,
        val costBuilder: SeminarBuilder.CostBuilder,
        val fullBuilder: SeminarBuilder
    ): SpeechManAction()
    class ChangeCRUEditable(val componentID: Int, val isEditable: Boolean): SpeechManAction()
    class CommitCRUObject(val componentID: Int): SpeechManAction()
    class DecodeImages(val requestID: Int, val imagesSource: List<Any>): SpeechManAction()
    class DeleteAppointment(val appointment: Appointment): SpeechManAction()
    class DeletePerson(val person: Person): SpeechManAction()
    class DeleteSeminar(val seminarID: Int): SpeechManAction()
    class MigrateSeminarCosting(val builder: SeminarBuilder,
        val newCosting: Seminar.CostingStrategy
    ): SpeechManAction()
    object NavigateBack: SpeechManAction()
    class NavigateError(val errorMessageResID: Int): SpeechManAction()
    class PublishSemAppointsBuilder(val builder: SeminarAppointsBuilder): SpeechManAction()
    class PublishSeminarBuilder(val builder: SeminarBuilder): SpeechManAction()
    class RequestDataLackInfos(val request: DataLackInfosRequest): SpeechManAction()
    class RequestSync(val request: SyncRequest): SpeechManAction()
    class RetrieveSeminarAppointsBuilder(val seminarID: Int): SpeechManAction()
    class SaveAppointment(val appoint: Appointment): SpeechManAction()
    class SaveCRUObject(val componentID: Int): SpeechManAction()
    class SaveRemoteData(val rd: RemoteData): SpeechManAction()
    class SaveSeminar(val builder: SeminarBuilder): SpeechManAction()
    class SaveSeminarAppoints(val builder: SeminarAppointsBuilder): SpeechManAction()
    class SelectImage: SpeechManAction()
    class SetAppointedSeminarsFilter(val filter: SeminarsFilter): SpeechManAction()
    class SetBackButtonLock(val isLocked: Boolean): SpeechManAction()
    class SetPeopleFilter(val filter: PeopleFilter): SpeechManAction()
    class SetSeminarsFilter(val filter: SeminarsFilter): SpeechManAction()
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
    class UpdatePerson(val person: Person): SpeechManAction()
    class UpdateSeminar(val seminar: Seminar): SpeechManAction()
}