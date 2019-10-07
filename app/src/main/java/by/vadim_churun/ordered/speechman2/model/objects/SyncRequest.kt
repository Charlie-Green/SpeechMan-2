package by.vadim_churun.ordered.speechman2.model.objects


class SyncRequest(
    val requestID: Int,
    val action: SyncRequest.RemoteAction,
    val ip: String )
{
    enum class RemoteAction { IMPORT, EXPORT }
}