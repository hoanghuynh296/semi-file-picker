package vn.semicolon.filepicker

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.audio_item_layout.view.*
import kotlinx.android.synthetic.main.image_item_layout.view.*
import kotlinx.android.synthetic.main.video_item_layout.view.*
import vn.semicolon.base.widget.adapter.BaseAdapter
import java.io.File

/**
 * Adapter
 */
class FileAdapter(
    var callback: OnItemsSelectChanged,
    var minSelect: Int = 1,
    var maxSelect: Int = 1
) :
    BaseAdapter<FileItemModel>() {
    private val mSelectedItems = ArrayList<FileItemModel>()
    fun getSelectedPaths(): ArrayList<String> {
        val result = ArrayList<String>(mSelectedItems.size)
        mSelectedItems.forEach {
            result.add(it.path)
        }
        return result
    }

    private val isMaximum: Boolean
        get() = mSelectedItems.size == maxSelect

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<FileItemModel> {
        when (viewType) {
            SemiFileManager.FileType.AUDIO -> return AudioViewHolder(
                createView(
                    R.layout.audio_item_layout,
                    parent
                )
            )
            SemiFileManager.FileType.VIDEO -> return VideoViewHolder(
                createView(
                    R.layout.video_item_layout,
                    parent
                )
            )
            SemiFileManager.FileType.IMAGE -> return ImageViewHolder(
                createView(
                    R.layout.image_item_layout,
                    parent
                )
            )
        }
        return ImageViewHolder(createView(R.layout.image_item_layout, parent))
    }

    override fun getItemViewType(position: Int): Int {
        return getItemAt(position)!!.type
    }

    inner class VideoViewHolder(v: View) : BaseViewHolder<FileItemModel>(v) {
        override fun bindData(data: FileItemModel) {
            if (maxSelect == 1) {
                itemView.videoItem_checkbox.visibility = View.GONE
                itemView.videoItem_radio.visibility = View.VISIBLE

                if (mSelectedItems.contains(data)) {
                    itemView.videoItem_radio.isChecked = true
                    itemView.videoItem_selectedText.text = "Đã chọn"
                    itemView.videoItem_selectedText.visibility = View.VISIBLE
                } else {
                    itemView.videoItem_radio.isChecked = false
                    itemView.videoItem_selectedText.visibility = View.GONE
                }
                itemView.videoItem_video.loadFromUrl(data.path)

                itemView.videoItem_radio.setOnCheckedChangeListener { _, checked ->
                    if (itemView.videoItem_radio.isPressed) {
                        if (checked) {
                            if (!mSelectedItems.contains(data)) {
                                itemView.videoItem_selectedText.visibility = View.VISIBLE
                                itemView.videoItem_selectedText.text = "Đã chọn"
                                if (mSelectedItems.isNotEmpty()) {
                                    val old = mSelectedItems[0]
                                    mSelectedItems.clear()
                                    notifyItemChanged(this@FileAdapter.data.indexOf(old))
                                }
                                mSelectedItems.add(data)
                            }
                        }
                        notifyItemsSelectedChanged()
                    }
                }
            } else {
                if (mSelectedItems.contains(data)) {
                    itemView.videoItem_checkbox.isChecked = true
                    itemView.videoItem_selectedText.text =
                        (mSelectedItems.indexOf(data) + 1).toString()
                    itemView.videoItem_selectedText.visibility = View.VISIBLE
                } else {
                    itemView.videoItem_checkbox.isChecked = false
                    itemView.videoItem_selectedText.visibility = View.GONE
                }
                itemView.videoItem_video.loadFromUrl(data.path)

                itemView.videoItem_checkbox.setOnCheckedChangeListener { _, checked ->
                    if (itemView.videoItem_checkbox.isPressed) {
                        if (checked) {
                            if (isMaximum) {
                                itemView.videoItem_checkbox.isChecked = false
                                Toast.makeText(
                                    itemView.context,
                                    "Tối đa $maxSelect",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnCheckedChangeListener
                            } else if (!mSelectedItems.contains(data)) {
                                mSelectedItems.add(data)
                                onItemSelectChanged(checked, mSelectedItems.indexOf(data) + 1)
                            }
                        } else {
                            if (mSelectedItems.contains(data)) {
                                unselect(data)
                            }
                        }
                        notifyItemsSelectedChanged()
                    }
                }
            }
        }

        private fun unselect(item: FileItemModel) {
            val index = mSelectedItems.indexOf(item)
            mSelectedItems.remove(item)
            onItemSelectChanged(false, index + 1)
            notifyDataChanged(index)
        }

        private fun notifyItemsSelectedChanged() {
            callback.onItemsSelectChanged(mSelectedItems.size)
        }

        private fun onItemSelectChanged(isSelected: Boolean, number: Int) {
            itemView.videoItem_checkbox.isChecked = isSelected
            if (isSelected)
                itemView.videoItem_selectedText.text = number.toString()
            itemView.videoItem_selectedText.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

        private fun notifyDataChanged(from: Int) {
            for (i in from until mSelectedItems.size) {
                val index = data.indexOf(mSelectedItems[i])
                notifyItemChanged(index)
            }
        }
    }

    inner class AudioViewHolder(v: View) : BaseViewHolder<FileItemModel>(v) {

        override fun bindData(data: FileItemModel) {
            itemView.audioItem_name.text = data.path.split(File.separator).last()
            // init single choice
            if (maxSelect == 1) {

                itemView.audioItem_checkbox.visibility = View.GONE
                itemView.audioItem_radio.visibility = View.VISIBLE

                itemView.audioItem_radio.isChecked = mSelectedItems.contains(data)

                itemView.audioItem_radio.setOnCheckedChangeListener { _, checked ->
                    if (itemView.audioItem_radio.isPressed) {
                        if (checked) {
                            if (!mSelectedItems.contains(data)) {
                                if (mSelectedItems.isNotEmpty()) {
                                    val old = mSelectedItems[0]
                                    mSelectedItems.clear()
                                    notifyItemChanged(this@FileAdapter.data.indexOf(old))
                                }
                                mSelectedItems.add(data)
                            }
                        }
                        notifyItemsSelectedChanged()
                    }
                }
            } else {
                itemView.audioItem_checkbox.isChecked = mSelectedItems.contains(data)
                itemView.audioItem_checkbox.setOnCheckedChangeListener { _, checked ->
                    if (itemView.audioItem_checkbox.isPressed) {


                        if (checked) {
                            if (isMaximum) {
                                itemView.audioItem_checkbox.isChecked = false
                                Toast.makeText(
                                    itemView.context,
                                    "Tối đa $maxSelect",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnCheckedChangeListener
                            } else if (!mSelectedItems.contains(data)) {
                                mSelectedItems.add(data)
                                onItemSelectChanged(checked, mSelectedItems.indexOf(data) + 1)
                            }
                        } else {
                            if (mSelectedItems.contains(data)) {
                                val index = mSelectedItems.indexOf(data)
                                mSelectedItems.remove(data)
                                onItemSelectChanged(checked, index + 1)
                                notifyDataChanged(index)
                            }
                        }
                        notifyItemsSelectedChanged()
                    }
                }
            }

        }

        private fun notifyItemsSelectedChanged() {
            callback.onItemsSelectChanged(mSelectedItems.size)
        }

        private fun onItemSelectChanged(isSelected: Boolean, number: Int) {
            itemView.audioItem_checkbox.isChecked = isSelected
        }

        private fun notifyDataChanged(from: Int) {
            for (i in from until mSelectedItems.size) {
                val index = data.indexOf(mSelectedItems[i])
                notifyItemChanged(index)
            }
        }
    }

    inner class ImageViewHolder(v: View) : BaseViewHolder<FileItemModel>(v) {
        override fun bindData(data: FileItemModel) {
            // init single choice
            if (maxSelect == 1) {

                itemView.imageItem_checkbox.visibility = View.GONE
                itemView.imageItem_radio.visibility = View.VISIBLE

                if (mSelectedItems.contains(data)) {
                    itemView.imageItem_radio.isChecked = true
                    itemView.imageItem_selectedText.text = "Đã chọn"
                    itemView.imageItem_selectedText.visibility = View.VISIBLE
                } else {
                    itemView.imageItem_radio.isChecked = false
                    itemView.imageItem_selectedText.visibility = View.GONE
                }
                itemView.imageItem_image.loadFromUrlAsThumbnail(
                    data.path,
                    listener = object : ImageLoadListener {
                        override fun onLoadFailed(e: Exception?) {
                            Log.e("FilePicker", "Can't load ${data.path} with error below")
                            Log.e("FilePicker", "Error ${e?.localizedMessage}")
                            if (adapterPosition >= 0)
                                removeAt(adapterPosition)
                        }

                        override fun onLoadSuccess(drawable: Drawable?) {

                        }

                    })

                itemView.imageItem_radio.setOnCheckedChangeListener { _, checked ->
                    if (itemView.imageItem_radio.isPressed) {
                        if (checked) {
                            if (!mSelectedItems.contains(data)) {
                                itemView.imageItem_selectedText.visibility = View.VISIBLE
                                itemView.imageItem_selectedText.text = "Đã chọn"
                                if (mSelectedItems.isNotEmpty()) {
                                    val old = mSelectedItems[0]
                                    mSelectedItems.clear()
                                    notifyItemChanged(this@FileAdapter.data.indexOf(old))
                                }
                                mSelectedItems.add(data)
                            }
                        }
                        notifyItemsSelectedChanged()
                    }
                }
                itemView.imageItem_checkboxWrapper.setOnClickListener {
                    itemView.imageItem_radio.isPressed = true
                    itemView.imageItem_radio.performClick()
                    itemView.imageItem_radio.isPressed = false
                }
            } else {
                if (mSelectedItems.contains(data)) {
                    itemView.imageItem_checkbox.isChecked = true
                    itemView.imageItem_selectedText.text =
                        (mSelectedItems.indexOf(data) + 1).toString()
                    itemView.imageItem_selectedText.visibility = View.VISIBLE
                } else {
                    itemView.imageItem_checkbox.isChecked = false
                    itemView.imageItem_selectedText.visibility = View.GONE
                }
                itemView.imageItem_image.loadFromUrlAsThumbnail(
                    data.path,
                    listener = object : ImageLoadListener {
                        override fun onLoadFailed(e: Exception?) {
                            Log.e("FilePicker", "Can't load ${data.path} with error below")
                            Log.e("FilePicker", "Error ${e?.localizedMessage}")
                            if (adapterPosition >= 0)
                                removeAt(adapterPosition)
                        }

                        override fun onLoadSuccess(drawable: Drawable?) {

                        }

                    })

                itemView.imageItem_checkbox.setOnCheckedChangeListener { _, checked ->
                    if (itemView.imageItem_checkbox.isPressed) {
                        if (checked) {
                            if (isMaximum) {
                                itemView.imageItem_checkbox.isChecked = false
                                Toast.makeText(
                                    itemView.context,
                                    "Tối đa $maxSelect",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnCheckedChangeListener
                            } else if (!mSelectedItems.contains(data)) {
                                mSelectedItems.add(data)
                                onItemSelectChanged(checked, mSelectedItems.indexOf(data) + 1)
                            }
                        } else {
                            if (mSelectedItems.contains(data)) {
                                val index = mSelectedItems.indexOf(data)
                                mSelectedItems.remove(data)
                                onItemSelectChanged(checked, index + 1)
                                notifyDataChanged(index)
                            }
                        }
                        notifyItemsSelectedChanged()
                    }
                }
                itemView.imageItem_checkboxWrapper.setOnClickListener {
                    itemView.imageItem_checkbox.isPressed = true
                    itemView.imageItem_checkbox.performClick()
                    itemView.imageItem_checkbox.isPressed = false
                }
            }

        }

        private fun notifyItemsSelectedChanged() {
            callback.onItemsSelectChanged(mSelectedItems.size)
        }

        private fun onItemSelectChanged(isSelected: Boolean, number: Int) {
            if (maxSelect == 1)
                itemView.imageItem_radio.isChecked = isSelected
            else
                itemView.imageItem_checkbox.isChecked = isSelected
            if (isSelected)
                itemView.imageItem_selectedText.text = number.toString()
            itemView.imageItem_selectedText.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

        private fun notifyDataChanged(from: Int) {
            for (i in from until mSelectedItems.size) {
                val index = data.indexOf(mSelectedItems[i])
                notifyItemChanged(index)
            }
        }
    }

    interface OnItemsSelectChanged {
        fun onItemsSelectChanged(size: Int)
    }
}
