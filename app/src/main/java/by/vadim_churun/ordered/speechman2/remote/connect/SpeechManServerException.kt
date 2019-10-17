package by.vadim_churun.ordered.speechman2.remote.connect

import java.io.IOException


class SpeechManServerException(
    private val errorCode: Byte
): IOException("Server responsed an error. Code: $errorCode")
{
    enum class Reason {
        NO_DATA,
        UNKNOWN_REMOTE_ACTION
    }

    val reason: Reason
        get() {
            when(errorCode.toInt())
            {
                1    -> return Reason.NO_DATA
                16   -> return Reason.UNKNOWN_REMOTE_ACTION
                else -> throw Exception("Unknown errorCode $errorCode")
            }
        }
}