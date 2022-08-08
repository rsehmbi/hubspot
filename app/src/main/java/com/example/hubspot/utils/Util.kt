/* This util file is adapted from the course website:
*  https://www.sfu.ca/~xingdong/Teaching/CMPT362/code/Kotlin_code/CameraDemoKotlin.zip */

package com.example.hubspot.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Util {
    fun checkCameraAndStoragePermissions(activity: Activity?): Boolean {
        if (Build.VERSION.SDK_INT < 23) return true

        val requestCode = 0
        val permissions =
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )

        if (activity == null) {
            return false
        }

        if (checkExternalStoragePerm(activity) != PackageManager.PERMISSION_GRANTED
            || checkCameraPerm(activity) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                requestCode
            )
            return false
        }
        return true
    }

    private fun checkCameraPerm(activity: Activity) =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)

    private fun checkExternalStoragePerm(activity: Activity) =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun getBitmap(context: Context, imgUri: Uri, shouldRotate: Boolean = true): Bitmap {
        val inputStream = context.contentResolver.openInputStream(imgUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val matrix = getCorrectBitmapMatrix(imgUri, context)
        val ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }

    // Adapted from https://stackoverflow.com/q/31925712
    /** Correctly rotate bitmap according to image orientation in exif data */
    private fun getCorrectBitmapMatrix(imgUri: Uri, context: Context): Matrix {
        val matrix = Matrix()

        val imageInputStream = context.contentResolver.openInputStream(imgUri)

        if (imageInputStream == null) {
            // Default fallback to 90 degrees
            matrix.setRotate(90f)
            return matrix
        }

        val exifInterface = ExifInterface(imageInputStream)

        // Detect how much the image needs to rotate
        val imageOrientation: Int = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        // Rotate the image rotation matrix by the detected amount
        when (imageOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 ->
                matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 ->
                matrix.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 ->
                matrix.setRotate(270f)
            else -> matrix.setRotate(0f)
        }

        return matrix
    }
}