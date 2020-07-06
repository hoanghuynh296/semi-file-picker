package vn.semicolon.filepicker

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import io.reactivex.Flowable
import java.io.File

object SemiFileManager {
    object FileType {
        const val IMAGE = 0
        const val VIDEO = 1
        const val AUDIO = 2
    }

    object OrderBy {
        val DATA = MediaStore.MediaColumns.DATA
        val SIZE = MediaStore.MediaColumns.SIZE
        val DISPLAY_NAME = MediaStore.MediaColumns.DISPLAY_NAME
        val TITLE = MediaStore.MediaColumns.TITLE
        val DATE_ADDED = MediaStore.MediaColumns.DATE_ADDED
        val DATE_MODIFIED = MediaStore.MediaColumns.DATE_MODIFIED
        val MIME_TYPE = MediaStore.MediaColumns.MIME_TYPE
        val WIDTH = MediaStore.MediaColumns.WIDTH
        val HEIGHT = MediaStore.MediaColumns.HEIGHT
    }

    fun getImagesFilePath(cr: ContentResolver): Flowable<List<FileItemModel>> {
        return getFilePath(FileType.IMAGE, cr)
    }

    fun getAudiosFilePath(cr: ContentResolver): Flowable<List<FileItemModel>> {
        return getFilePath(FileType.AUDIO, cr)
    }

    fun getVideosFilePath(cr: ContentResolver): Flowable<List<FileItemModel>> {
        return getFilePath(FileType.VIDEO, cr)
    }

    private fun getFilePath(
        type: Int,
        cr: ContentResolver,
        orderBy: String = OrderBy.DATE_ADDED
    ): Flowable<List<FileItemModel>> {
        var columns: Array<String>? = null
        var uri: Uri? = null
        var columnIndex: String? = null
        when (type) {
            FileType.VIDEO -> {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                columns = arrayOf(MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID)
                columnIndex = MediaStore.Video.Media.DATA
            }
            FileType.AUDIO -> {
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                columns = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media._ID)
                columnIndex = MediaStore.Audio.Media.DATA
            }
            FileType.IMAGE -> {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
                columnIndex = MediaStore.Images.Media.DATA
            }
        }
        return Flowable.defer {
            val cursor = cr.query(
                uri!!, columns,
                null, null, orderBy
            )
            val count = cursor!!.count
            val resultItem = ArrayList<FileItemModel>(count)
            for (i in 0 until count) {
                cursor.moveToPosition(i)
                val dataColumnIndex = cursor.getColumnIndex(columnIndex)
                //Store the path of the image
                val path = cursor.getString(dataColumnIndex)
                val exist = File(path).exists()
                if (exist)
                    resultItem.add(FileItemModel(path, type))
            }
            cursor.close()
            Flowable.just(resultItem.reversed())
        }
    }
}
