package by.vadim_churun.ordered.speechman2.remote


class SpeechManXmlException: Exception
{
    constructor():
        super()
    constructor(message: String):
        super(message)
    constructor(message: String, cause: Throwable):
            super(message, cause)
}