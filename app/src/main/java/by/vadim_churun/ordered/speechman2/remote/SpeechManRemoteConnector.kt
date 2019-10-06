package by.vadim_churun.ordered.speechman2.remote

import java.net.*


object SpeechManRemoteConnector
{
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

    fun openConnection(ip: String): URLConnection
    {
        val url = URL("http://${ip}:8080/SpeechMan Server/SpeechMan2")
        return url.openConnection().apply {
            doInput = true
            doOutput = true
        }
    }
}