package com.dev_vlad.car_v.util

import android.graphics.Bitmap
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.FutureTarget
import java.io.ByteArrayOutputStream
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private const val TAG = "ImageCompressor"
private const val MAX_IMG_SIZE = 1080

suspend fun compressImage(glideRequestMgr: RequestManager, filePath: String?): ByteArray? {
    return try {
        if (filePath == null) {
            MyLogger.logThis(TAG, "compressImage", "file path was null")
            null
        } else {
            val futureTarget: FutureTarget<Bitmap> = glideRequestMgr
                    .asBitmap()
                    .load(filePath)
                    .submit(0, MAX_IMG_SIZE)
            val compressedBitmap = futureTarget.get()
            glideRequestMgr.clear(futureTarget)
            val baos = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val compressedBytes = baos.toByteArray()
            baos.reset()
            MyLogger.logThis(TAG, "compressImage", "new bytes size -- ${compressedBytes.size}")
            compressedBytes
        }
    } catch (e: Exception) {
        MyLogger.logThis(TAG, "compressImage", "exception raised ${e.message}", e)
        null
    }
}


