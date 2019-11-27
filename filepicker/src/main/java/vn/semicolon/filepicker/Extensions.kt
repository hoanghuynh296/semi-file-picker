package vn.semicolon.filepicker

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


fun ImageView.loadFromUrl(
    url: String?,
    centerCrop: Boolean = true,
    requestOptions: RequestOptions? = null,
    placeHolderResId: Int? = null,
    errorResId: Int? = null,
    listener: ImageLoadListener? = null
) {
    if (url == null) return
    var request = RequestOptions()
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .priority(Priority.HIGH)
    if (centerCrop)
        request = request.centerCrop()
    if (placeHolderResId != null) request.placeholder(placeHolderResId)
    if (errorResId != null) request.error(errorResId)
    var g = Glide.with(context.applicationContext)
        .load(url)
        .apply(request)
    if (requestOptions != null) {
        g = g.apply(requestOptions)
    }

    g.listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            listener?.onLoadFailed(e)
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            listener?.onLoadSuccess(resource)
            return false
        }
    })
        .into(this)

}

interface ImageLoadListener {
    fun onLoadFailed(e: Exception?)
    fun onLoadSuccess(drawable: Drawable?)
}


fun ImageView.loadFromUrlAsThumbnail(
    url: String?,
    centerCrop: Boolean = true,
    requestOptions: RequestOptions? = null,
    placeHolderResId: Int? = null,
    errorResId: Int? = null,
    listener: ImageLoadListener? = null
) {
    if (url == null) return
    var request = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .priority(Priority.HIGH)
    if (centerCrop)
        request = request.centerCrop()
    if (placeHolderResId != null) request.placeholder(placeHolderResId)
    if (errorResId != null) request.error(errorResId)
    var g = Glide.with(context.applicationContext)
        .load(url)
        .thumbnail(0.1f)
        .apply(request)
    if (requestOptions != null) {
        g = g.apply(requestOptions)
    }

    g.listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            listener?.onLoadFailed(e)
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            listener?.onLoadSuccess(resource)
            return false
        }

    })
        .into(this)

}
