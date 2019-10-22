package by.vadim_churun.ordered.speechman2.model.exceptions

import java.io.IOException


class UnknownResponseSpeechManException(response: Byte):
IOException("Received unknown response $response")