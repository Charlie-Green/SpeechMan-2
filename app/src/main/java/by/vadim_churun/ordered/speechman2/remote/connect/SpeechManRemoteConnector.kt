package by.vadim_churun.ordered.speechman2.remote.connect

import android.content.Context
import java.io.IOException
import java.net.*


object SpeechManRemoteConnector
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // IP:

    private const val IP_FILENAME = "ip.txt"

    fun validateIP(ip: String): Boolean
    {
        val octets = ip.split('.')
        if(octets.size != 4) return false
        for(octet in octets)
        {
            try {
                val number = octet.toInt()
                if(number < 0 || number > 255)
                    return false
            } catch(exc: NumberFormatException) {
                return false
            }
        }
        return true
    }

    fun persistIP(appContext: Context, ip: String)
    {
        appContext.openFileOutput(IP_FILENAME, Context.MODE_PRIVATE).use { outstream ->
            outstream.write(ip.toByteArray())
        }
    }

    fun getPersistedIP(appContext: Context): String?
    {
        try {
            val instream = appContext.openFileInput(IP_FILENAME)
            return String(instream.readBytes()).also { instream.close() }
        } catch(exc: IOException) {
            return null
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // CONNECTION:

    fun openConnection(ip: String): URLConnection
    {
        val url = URL("http://${ip}:8080/SpeechMan Server/SpeechMan2")
        return url.openConnection().apply {
            doInput = true
            doOutput = true
            connectTimeout = 6000
        }
    }
}