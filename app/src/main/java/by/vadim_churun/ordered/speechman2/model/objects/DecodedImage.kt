package by.vadim_churun.ordered.speechman2.model.objects

import android.content.ContentResolver
import android.graphics.*
import android.net.Uri


class DecodedImage(
    val requestID: Int,
    val listPosition: Int,
    val bitmap: Bitmap )
{
    companion object
    {
        fun from(requestID: Int, resolver: ContentResolver, listPosition: Int, uri: Uri?): DecodedImage?
        {
            uri ?: return null
            resolver.openInputStream(uri).use {
                val bitmap = BitmapFactory.decodeStream(it)
                bitmap ?: return null
                return DecodedImage(requestID, listPosition, bitmap)
            }
        }
    }
}