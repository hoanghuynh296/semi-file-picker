package vn.semicolon.filepicker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_preview_image.*
import vn.semicolon.filepicker.ImageUtils.getSize
import java.net.URL


class PreviewImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)
        val path: String? = intent.getStringExtra("data")

        if (path.isNullOrBlank()) {
            finish()
            return
        }

        previewImage_container.post {
            if (URLUtil.isHttpUrl(path) || URLUtil.isHttpsUrl(path))
                Glide.with(this)
                    .asBitmap()
                    .load(path)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val size = getResponsiveSize(
                                resource.width,
                                resource.height,
                                previewImage_image.width,
                                previewImage_image.height
                            )
                            val lp = FrameLayout.LayoutParams(size.first, size.second)
                            lp.gravity = Gravity.CENTER
                            previewImage_image.layoutParams = lp
                            val bitmap =
                                Bitmap.createScaledBitmap(resource, size.first, size.second, false)
                            previewImage_image.setImageBitmap(bitmap)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    })
            else {
                previewImage_container.let {
                    val size = getResponsiveSize(path!!, it.width, it.height)
                    val lp = FrameLayout.LayoutParams(size.first, size.second)
                    lp.gravity = Gravity.CENTER
                    previewImage_image.layoutParams = lp
                    previewImage_image.loadFromUrl(path, false)
                }
            }
        }
        previewImage_back.setOnClickListener {
            finish()
        }
    }

    companion object {
        fun start(context: Context, path: String) {
            val intent = Intent(context, PreviewImageActivity::class.java)
            intent.putExtra("data", path)
            context.startActivity(intent)
        }
    }


    private fun getResponsiveSize(path: String, maxWidth: Int, maxHeight: Int): Pair<Int, Int> {
        val size = getSize(path)
        return getResponsiveSize(size.first, size.second, maxWidth, maxHeight)
    }

    private fun getResponsiveSize(
        rawWidth: Int,
        rawHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        val rawRatio = rawWidth.toFloat() / rawHeight.toFloat()
        val maxRatio = maxWidth.toFloat() / maxHeight.toFloat()
        var w: Int = 0
        var h: Int = 0
        if (rawRatio >= maxRatio) {
            w = maxWidth
            h = (w / rawRatio).toInt()
        } else {
            h = maxHeight
            w = (h * rawRatio).toInt()
        }
        return Pair(w, h)
    }
}
