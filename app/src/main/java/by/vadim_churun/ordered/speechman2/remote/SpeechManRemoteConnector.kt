package by.vadim_churun.ordered.speechman2.remote

import java.io.*
import java.net.URL


object SpeechManRemoteConnector
{
    private fun ipToUrl(ip: String): URL
        // URL of a servlet ran under a Tomcat server at the given IP.
        = URL("http://${ip}:8080/SpeechManServer/SpeechManServlet")

    fun getInputStream(ip: String): InputStream
    {
        // Return stream over a pre-defined String (for testing purposes):
        val xml =
            "<?xml version=\"1.0\"?>" +
            "<speech2>" +
            "</speech2>"
        return ByteArrayInputStream(xml.toByteArray())
    }
}