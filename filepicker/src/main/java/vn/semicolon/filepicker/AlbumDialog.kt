package vn.semicolon.filepicker


import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.albums_item_layout.view.*
import vn.semicolon.base.widget.adapter.BaseAdapter
import vn.semicolon.base.widget.adapter.OnItemClickListener

@Parcelize
data class FileItemModel(
    val path: String,
    val type: Int
) : Parcelable {
    fun isImage() = type == SemiFileManager.FileType.IMAGE
    fun isVideo() = type == SemiFileManager.FileType.VIDEO
    fun isAudio() = type == SemiFileManager.FileType.AUDIO
}

@Parcelize
data class AlbumModel(
    val name: String,
    val items: ArrayList<FileItemModel>
) : Parcelable

internal class AlbumDialog(
    var lifecycleOwner: LifecycleOwner?,
    var parent: ViewGroup?,
    width: Int,
    var callback: OnAlbumSelectedListener?
) : LifecycleObserver {

    private var popup: PopupWindow = PopupWindow(parent?.context).apply {
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.width = ViewGroup.LayoutParams.WRAP_CONTENT
    }
    private var recyclerView: RecyclerView
    private var archor: View? = null
    private var isLastShow: Boolean = false
    private var isDismissByUser: Boolean = true

    var isShowing = false
        get() {
            field = popup.isShowing
            return field
        }

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
        popup.contentView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.fragment_albums_dialog, parent, false)
        recyclerView = popup.contentView.findViewById(R.id.albums_data)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        popup.width = width
        popup.isOutsideTouchable = true
        popup.elevation = 0f
        popup.setOnDismissListener {
            isLastShow = !isDismissByUser
            mOnDialogVisibleChangedListener?.onDialogVisibileChanged(false)
        }
    }

    private var mOnDialogVisibleChangedListener: OnDialogVisibleChangedListener? = null

    interface OnDialogVisibleChangedListener {
        fun onDialogVisibileChanged(isShowing: Boolean)
    }

    fun setOnVisibleChangedListener(callback: OnDialogVisibleChangedListener) {
        mOnDialogVisibleChangedListener = callback
    }

    fun setData(data: List<AlbumModel>) {
        recyclerView.adapter = AlbumAdapter(data, object : OnItemClickListener<AlbumModel> {
            override fun onItemClick(item: AlbumModel?, pos: Int, view: View) {
                callback?.onAlbumSelected(item!!)
                dismiss()
            }
        })
    }

    fun show(archor: View) {
        this.archor = archor
        popup.showAsDropDown(archor)
        mOnDialogVisibleChangedListener?.onDialogVisibileChanged(true)
    }

    fun dismiss() {
        popup.dismiss()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onViewPaused() {
        if (isLastShow) {
            isDismissByUser = false
            popup.dismiss()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onViewStopped() {
        if (isLastShow) {
            isDismissByUser = false
            popup.dismiss()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onViewResume() {
        if (isLastShow && !isDismissByUser) {
            if (archor != null)
                show(archor!!)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onViewDestroyed() {
        callback = null
        lifecycleOwner = null
        parent = null
    }


    interface OnAlbumSelectedListener {
        fun onAlbumSelected(album: AlbumModel)
    }


    class AlbumAdapter(data: List<AlbumModel>, callback: OnItemClickListener<AlbumModel>) :
        BaseAdapter<AlbumModel>() {
        init {
            setOnItemClickListener(callback)
            addAll(data)
        }

        private var mSelected: Int = 0

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseViewHolder<AlbumModel> {
            return ViewHolder(createView(R.layout.albums_item_layout, parent))
        }

        override fun onItemClick(item: AlbumModel?, pos: Int, view: View) {
            super.onItemClick(item, pos, view)
            val pre = mSelected
            mSelected = pos
            notifyItemChanged(pre)
            view.albumsItem_radio.isChecked = true
        }

        inner class ViewHolder(v: View) : BaseViewHolder<AlbumModel>(v) {
            override fun bindData(data: AlbumModel) {
                itemView.albumsItem_radio.isChecked = mSelected == adapterPosition
                itemView.albumsItem_name.text = data.name
                itemView.albumsItem_count.text = data.items.size.toString()
                if (data.items.isEmpty()) return
                val firstItem = data.items[0]
                when {
                    firstItem.isImage() -> {
                        itemView.albumsItem_icon.loadFromUrl(data.items[0].path)
                        itemView.albumsItem_icon.setPadding(0, 0, 0, 0)
                        itemView.albumsItem_iconPlay.visibility = View.GONE
                    }
                    firstItem.isAudio() -> {
                        val dp =
                            (itemView.resources.getDimension(R.dimen._12sdp) / itemView.resources.displayMetrics.density).toInt()
                        itemView.albumsItem_icon.setPadding(dp, dp, dp, dp)
                        itemView.albumsItem_icon.setImageDrawable(itemView.context.getDrawable(R.drawable.ic_music_note_black_24dp))
                        itemView.albumsItem_iconPlay.visibility = View.GONE
                    }
                    firstItem.isVideo() -> {
                        itemView.albumsItem_icon.setPadding(0, 0, 0, 0)
                        itemView.albumsItem_icon.loadFromUrl(data.items[0].path)
                        itemView.albumsItem_iconPlay.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
