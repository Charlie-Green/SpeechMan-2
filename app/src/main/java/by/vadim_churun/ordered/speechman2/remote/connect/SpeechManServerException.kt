package by.vadim_churun.ordered.speechman2.remote.connect

import java.io.IOException


class SpeechManServerException(
    private val errorCode: Byte
): IOException("Server responsed an error. Code: $errorCode")
{
    enum class Reason {
        NO_DATA,
        UNKNOWN_REMOTE_ACTION,
        IO_EXCEPTION
    }

    val reason: Reason
        get() {
            when(errorCode.toInt())
            {
                1    -> return Reason.NO_DATA
                16   -> return Reason.UNKNOWN_REMOTE_ACTION
                32   -> return Reason.IO_EXCEPTION
                else -> throw Exception("Unknown errorCode $errorCode")
            }
        }
}