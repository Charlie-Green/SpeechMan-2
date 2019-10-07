package by.vadim_churun.ordered.speechman2.model.objects


class SyncResponse(val requestID: Int, val action: SyncResponse.ProgressStatus)
{
    enum class ProgressStatus {
        CONNECTION_OPENED,
        DATA_PUSHED,
        XML_PARSED
    }
}