package vn.semicolon.filepicker

import android.graphics.BitmapFactory
import android.media.ExifInterface
import java.io.File
import java.io.FileNotFoundException

internal object ImageUtils {
    fun getSize(path: String): Pair<Int, Int> {
        fun getSizeWithoutExif(): Pair<Int, Int> {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(File(path).absolutePath, options)
            val h = options.outHeight
            val w = options.outWidth
            return Pair(w, h)
        }

        val exif = getExif(path)
        var w = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
        var h = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
        if (w == 0 || h == 0) return getSizeWithoutExif()
        val o =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        if (o == ExifInterface.ORIENTATION_ROTATE_90 || o == ExifInterface.ORIENTATION_ROTATE_270) {
            val temp = w
            w = h
            h = temp
        }

        return Pair(w, h)
    }

    fun getExif(path: String): ExifInterface {
        val file = File(path)
        if (file.exists()) {
            return ExifInterface(path)
        } else throw FileNotFoundException("Path $path not found")
    }
}