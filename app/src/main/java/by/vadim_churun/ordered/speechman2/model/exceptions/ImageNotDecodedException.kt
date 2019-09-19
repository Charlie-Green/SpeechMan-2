package by.vadim_churun.ordered.speechman2.model.exceptions

import android.net.Uri


class ImageNotDecodedException(
    val requestID: Int,
    val listPosition: Int,
    val uri: Uri
): Exception("Failed to decode image by Uri $uri")