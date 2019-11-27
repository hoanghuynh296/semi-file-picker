package vn.semicolon.filepicker

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.util.ArrayMap
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_file_picker.*
import kotlinx.android.synthetic.main.audio_item_layout.view.*
import kotlinx.android.synthetic.main.image_item_layout.view.*
import kotlinx.android.synthetic.main.video_item_layout.view.*
import vn.semicolon.base.widget.adapter.BaseAdapter
import vn.semicolon.base.widget.adapter.OnItemClickListener
import java.io.File

class FilePicker : AppCompatActivity(), FileAdapter.OnItemsSelectChanged,
    AlbumDialog.OnAlbumSelectedListener,
    AlbumDialog.OnDialogVisibleChangedListener, OnItemClickListener<FileItemModel> {

    companion object {

        fun getResult(data: Intent?): Array<String> {
            return if (data == null) emptyArray()
            else data.getStringArrayExtra(RESULT)
        }

        @IntDef(TYPE_IMAGE, TYPE_AUDIO)
        @Retention(AnnotationRetention.SOURCE)
        annotation class FileType

        @IntDef(TYPE_IMAGE, TYPE_AUDIO)
        @Retention(AnnotationRetention.SOURCE)
        annotation class DisplayType

        const val TYPE_IMAGE = 0
        const val TYPE_AUDIO = 1
        const val TYPE_VIDEO = 2

        private const val TYPE_GRID = 0
        private const val TYPE_LINEAR_VERTICAL = 1

        const val RESULT = "result"

        private const val IMAGE_SETTING = "image_setting"
        private const val MAX_SELECT = "max_select"
        private const val MIN_SELECT = "min_select"
        private const val FILE_TYPES = "file_types"
        private const val CUSTOM_ALBUMS = "custom_albums"
        private const val COLUMN_COUNT = "column_count"
    }

    @FileType
    private var mFileTypes: HashSet<Int> = HashSet(TYPE_IMAGE)
    private var mMaxSelect: Int = 1
    private var mMinSelect: Int = 1
    private var mColumnCount: Int = 3
    private var mImageSetting: ImageSetting? = null

    private val mSelectedItem: List<String>
        get() {
            return mAdapter!!.getSelectedPaths()
        }
    private var mAdapter: FileAdapter? = null
    private var mAlbums: ArrayList<AlbumModel> = ArrayList()

    override fun onItemClick(item: FileItemModel?, pos: Int, view: View) {
        item?.let {
            when {
                it.isVideo() -> previewVideo(it.path)
                it.isAudio() -> previewAudio(it.path)
                it.isImage() -> previewImage(it.path)
            }
        }
    }

    private fun previewImage(path: String) {
        PreviewImageActivity.start(this, path)
    }

    private var mMediaPlayer: MediaPlayer? = null
    private var mLasPath: String? = null
    private fun previewVideo(path: String) {}
    private fun previewAudio(path: String) {
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.isPlaying)
                mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        val dis = Flowable.fromCallable {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setDataSource(path)
            mMediaPlayer!!.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            mMediaPlayer!!.prepare()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                mMediaPlayer!!.start()
            }
    }

    override fun onDialogVisibileChanged(isShowing: Boolean) {
        filePicker_shadow.visibility = if (isShowing) View.VISIBLE else View.GONE
    }

    override fun onAlbumSelected(album: AlbumModel) {
        filePicker_albums.text = album.name
        mAdapter?.clear()
        mAdapter?.addAll(album.items)
    }


    override fun onItemsSelectChanged(size: Int) {
        filePicker_count.text = if (size > 0) getString(
            R.string.text_selected_count,
            size
        ) else getString(R.string.text_no_file_selected)
        filePicker_submit.isEnabled = (size > 0 && size >= mMinSelect && size <= mMaxSelect)
    }

    private fun initTheme() {
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        @ColorInt val color = typedValue.data
        window.statusBarColor = color
    }

    private fun initUI() {
        (filePicker_data.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        mAdapter = FileAdapter(this, mMinSelect, mMaxSelect)
        mAdapter!!.setOnItemClickListener(this)
        filePicker_data.adapter = mAdapter
        initPicker(mFileTypes)

        filePicker_submit.setOnClickListener {
            val intent = Intent()
            intent.putExtra("result", mSelectedItem.toTypedArray())
            setResult(Activity.RESULT_OK, intent)
            onBackPressed()
        }
        filePicker_cancel.setOnClickListener {
            onBackPressed()
        }
        filePicker_albums.setOnClickListener {
            showOrDismissAlbumDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTheme()
        setContentView(R.layout.activity_file_picker)
        initSetting()
        initUI()
    }

    private var mAlbumDialog: AlbumDialog? = null
    private fun showOrDismissAlbumDialog() {
        if (mAlbumDialog == null) {
            mAlbumDialog = AlbumDialog(
                this,
                filePicker_root as ViewGroup,
                filePicker_toolbar.width,
                this
            )
            mAlbumDialog!!.setData(mAlbums)
            mAlbumDialog!!.setOnVisibleChangedListener(this)
            mAlbumDialog!!.show(filePicker_toolbar)
        } else if (!mAlbumDialog!!.isShowing) {
            mAlbumDialog!!.show(filePicker_toolbar)
        } else if (mAlbumDialog!!.isShowing) {
            mAlbumDialog!!.dismiss()
        }
    }

    private fun initPicker(@FileType types: HashSet<Int>) {
        when {
            types.contains(TYPE_IMAGE) -> initImagePicker()
            types.contains(TYPE_VIDEO) -> initVideoPicker()
            types.contains(TYPE_AUDIO) -> initAudioPicker()
        }
    }

    private fun initSetting() {
        val fileTypes = intent.getIntArrayExtra(FILE_TYPES)
        if (fileTypes != null && fileTypes.isNotEmpty()) {
            mFileTypes.clear()
            mFileTypes.addAll(fileTypes.toList())
        }

        mMaxSelect = intent.getIntExtra(MAX_SELECT, mMaxSelect)
        mMinSelect = intent.getIntExtra(MIN_SELECT, mMinSelect)
        if (mFileTypes.contains(TYPE_IMAGE)) {
            mImageSetting = intent.getParcelableExtra(IMAGE_SETTING)
        }

        mColumnCount = intent.getIntExtra(COLUMN_COUNT, mColumnCount)
    }


    private fun initRecyclerDisplay(rv: RecyclerView, @DisplayType type: Int) {
        fun getHorizontalDivider(): DividerItemDecoration {
            val hDivider = DividerItemDecoration(
                this, LinearLayout.HORIZONTAL
            )
            hDivider.setDrawable(getDrawable(R.drawable.item_horization_devider)!!)
            return hDivider
        }

        fun getVerticalDivider(): DividerItemDecoration {
            val vDivider = DividerItemDecoration(
                this, LinearLayout.VERTICAL
            )
            vDivider.setDrawable(getDrawable(R.drawable.item_vertical_devider)!!)
            return vDivider
        }

        fun getGridLayoutManager(): RecyclerView.LayoutManager {
            val lm = GridLayoutManager(this@FilePicker, mColumnCount)
            return lm
        }

        fun getLinearLayoutManager(): RecyclerView.LayoutManager {
            val lm = LinearLayoutManager(this)
            lm.orientation = RecyclerView.VERTICAL
            return lm
        }

        when (type) {
            TYPE_GRID -> {
                rv.layoutManager = getGridLayoutManager()
                rv.addItemDecoration(getVerticalDivider())
                rv.addItemDecoration(getHorizontalDivider())
            }
            TYPE_LINEAR_VERTICAL -> {
                rv.layoutManager = getLinearLayoutManager()
                rv.addItemDecoration(getVerticalDivider())
            }
        }

    }

    private fun getCustomAlbums(): ArrayList<AlbumModel>? {
        return intent.getParcelableArrayListExtra(CUSTOM_ALBUMS)
    }

    private fun getAlbums(paths: List<FileItemModel>): List<AlbumModel> {
        fun getAlbumName(path: String): String {
            val arr = path.split(File.separator)
            return if (arr.size >= 2)
                arr[arr.size - 2]
            else "No name"
        }

        val data = ArrayMap<String, ArrayList<FileItemModel>>()
        val allAlbum = AlbumModel("Tất cả", ArrayList())
        paths.forEach {
            val albumName = getAlbumName(it.path)
            allAlbum.items.add(it)
            if (data.containsKey(albumName)) {
                data[albumName]!!.add(it)
            } else {
                val value = ArrayList<FileItemModel>()
                value.add(it)
                data[albumName] = value
            }
        }
        val result = ArrayList<AlbumModel>(data.size)
        getCustomAlbums()?.let {
            result.addAll(it)
        }
        result.add(allAlbum)
        data.forEach {
            result.add(AlbumModel(it.key, it.value))
        }
        return result
    }

    private fun getAllAudioPath(): Flowable<List<FileItemModel>> {
        return SemiFileManager.getAudiosFilePath(contentResolver)
    }

    private fun getAllVideoPath(): Flowable<List<FileItemModel>> {
        return SemiFileManager.getVideosFilePath(contentResolver)
    }

    private fun getAllImagePath(): Flowable<List<FileItemModel>> {
        return SemiFileManager.getImagesFilePath(contentResolver)
    }

    private val mDisposables = ArrayList<Disposable>()

    private fun initAudioPicker() {
        val dis = getAllAudioPath().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val arrPath = it
                mAlbums.addAll(getAlbums(arrPath))
                if (mAlbums.isNotEmpty())
                    mAdapter?.addAll(mAlbums[0].items)

                filePicker_data.adapter = mAdapter
                val vDivider = DividerItemDecoration(
                    this, LinearLayout.VERTICAL
                )
                vDivider.setDrawable(getDrawable(R.drawable.item_vertical_devider)!!)
                filePicker_data.addItemDecoration(vDivider)
                val lm = LinearLayoutManager(this)
                lm.orientation = RecyclerView.VERTICAL
                filePicker_data.layoutManager = lm
                (filePicker_data.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
                    false
            }, {
                Log.e("initVideoPicker", it.localizedMessage)
            })
        mDisposables.add(dis)
    }

    private fun initVideoPicker() {
        initRecyclerDisplay(filePicker_data, TYPE_GRID)
        val dis = getAllVideoPath().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val arrPath = it
                mAlbums.addAll(getAlbums(arrPath))
                if (mAlbums.isNotEmpty())
                    mAdapter?.addAll(mAlbums[0].items)
            }, {
                Log.e("initVideoPicker", it.localizedMessage)
            })
        mDisposables.add(dis)
    }

    private fun initImagePicker() {
        initRecyclerDisplay(filePicker_data, TYPE_GRID)
        val dis = getAllImagePath().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val arrPath = it
                mAlbums.addAll(getAlbums(arrPath))
                if (mAlbums.isNotEmpty())
                    mAdapter?.addAll(mAlbums[0].items)
            }, {

            })
        mDisposables.add(dis)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mDisposables.forEach {
            if (!it.isDisposed)
                it.dispose()
        }
        if (mMediaPlayer?.isPlaying == true) {
            mMediaPlayer?.stop()
        }
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    /**
     * @param scale in 0..N
     */
    @Parcelize
    data class ImageSetting(
        var scale: Double = 1.0
    ) : Parcelable

    class Builder {
        @FileType
        private var mFileTypes: HashSet<Int> = HashSet(TYPE_IMAGE)
        private var mMinSelect: Int = 1
        private var mMaxSelect: Int = 1
        private var mColumnCount: Int = 3
        private var mImageSetting: ImageSetting? = null
        private var mCustomAlbums: ArrayList<AlbumModel> = ArrayList()
        /**
         * @see FileType
         */
        fun typesOf(@FileType vararg fileTypes: Int): Builder {
            mFileTypes.clear()
            mFileTypes.addAll(fileTypes.toList())
            return this
        }

        fun addImageAlbum(albumName: String, vararg filePath: String): Builder {
            return addAlbum(albumName, TYPE_IMAGE, *filePath)
        }

        fun addVideoAlbum(albumName: String, vararg filePath: String): Builder {
            return addAlbum(albumName, TYPE_VIDEO, *filePath)
        }

        fun coloumnCount(howMany: Int): Builder {
            if (howMany > 0)
                mColumnCount = howMany
            return this
        }

        fun addAudioAlbum(albumName: String, vararg filePath: String): Builder {
            return addAlbum(albumName, TYPE_AUDIO, *filePath)
        }

        /**
         * Add custom album to file picker, useful when you save a list of file
         * such as "favorite images, last images,.." for your app
         */
        fun addAlbum(albumName: String, @FileType type: Int, vararg filePath: String): Builder {
            val items = ArrayList<FileItemModel>(filePath.size)
            filePath.forEach {
                val f = FileItemModel(path = it, type = type)
                items.add(f)
            }
            mCustomAlbums.add(AlbumModel(name = albumName, items = items))
            return this
        }

        fun imageSetting(setting: ImageSetting): Builder {
            mImageSetting = setting
            return this
        }

        fun maxSelect(howMany: Int): Builder {
            mMaxSelect = howMany
            return this
        }

        fun minSelect(howMany: Int): Builder {
            mMinSelect = howMany
            return this
        }

        fun start(activity: Activity, requestCode: Int) {
            val intent = Intent(activity, FilePicker::class.java)

            putData(intent)

            activity.startActivityForResult(intent, requestCode)
        }

        fun start(fragment: Fragment, requestCode: Int) {
            val intent = Intent(fragment.context, FilePicker::class.java)

            putData(intent)

            fragment.startActivityForResult(intent, requestCode)
        }

        private fun putData(intent: Intent) {
            if (mFileTypes.contains(TYPE_IMAGE) && mImageSetting != null) {
                intent.putExtra(IMAGE_SETTING, mImageSetting)
            }

            intent.putExtra(MAX_SELECT, mMaxSelect)
            intent.putExtra(MIN_SELECT, mMinSelect)
            intent.putExtra(FILE_TYPES, mFileTypes.toIntArray())
            intent.putExtra(CUSTOM_ALBUMS, mCustomAlbums)
            intent.putExtra(COLUMN_COUNT, mColumnCount)
        }
    }

}

