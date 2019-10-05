package by.vadim_churun.ordered.speechman2.remote.xml

import java.io.OutputStream
import java.io.PrintWriter


class SpeechManXmlWriter
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // PROPERTIES, CONSTRUCTORS:

    private val out: PrintWriter

    constructor(outstream: OutputStream)
    {
        out = PrintWriter(outstream)
    }


    /** If true, the resulting XML file is more human readable, but is widely larger in its size. **/
    var indent = false


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP METHODS:

    private var tabs: MutableList<String>? = null

    private fun getTabs(count: Int): String
    {
        if(!indent) return ""

        val mTabs = tabs ?: mutableListOf(System.lineSeparator()).also { tabs = it }
        while(mTabs.size < count)
        {
            mTabs.add(mTabs.last() + "\t")
        }
        // mTabs[j] contains the line separator + j tabs.
        return mTabs[count]
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // HEAD AND ENDING:

    fun writeHead()
    {
        out.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        out.print("${getTabs(0)}<${XmlContract.TAG_ROOT}>")
    }

    fun writeEnding()
    { out.println("${getTabs(0)}</${XmlContract.TAG_ROOT}>") }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // ENTITIES:
}