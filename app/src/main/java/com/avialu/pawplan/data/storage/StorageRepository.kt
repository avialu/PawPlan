package com.avialu.pawplan.data.storage

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.avialu.pawplan.data.firebase.FirebaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class StorageRepository {

    private val storage = FirebaseProvider.storage

    suspend fun uploadUserProfileImage(
        userId: String,
        imageUri: Uri,
        contentResolver: ContentResolver
    ): String {
        val bytes = compressImage(contentResolver, imageUri)

        val ref = storage.reference
            .child("users")
            .child(userId)
            .child("profile.jpg")

        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    private suspend fun compressImage(
        contentResolver: ContentResolver,
        uri: Uri
    ): ByteArray = withContext(Dispatchers.IO) {
        val input = contentResolver.openInputStream(uri) ?: error("Cannot open image")
        val bitmap = BitmapFactory.decodeStream(input)
        input.close()

        val scaled = scaleDown(bitmap, maxSize = 1080)

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
        out.toByteArray()
    }

    private fun scaleDown(src: Bitmap, maxSize: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w <= maxSize && h <= maxSize) return src

        val ratio = if (w >= h) maxSize.toFloat() / w else maxSize.toFloat() / h
        val nw = (w * ratio).toInt()
        val nh = (h * ratio).toInt()
        return Bitmap.createScaledBitmap(src, nw, nh, true)
    }
}