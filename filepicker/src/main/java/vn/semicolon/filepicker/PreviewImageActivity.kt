package vn.semicolon.filepicker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_preview_image.*
import vn.semicolon.filepicker.ImageUtils.getSize


class PreviewImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)
        val path: String? = intent.getStringExtra("data")

        if (path.isNullOrBlank()) finish()

        previewImage_container.post {
            previewImage_container.let {
                val size = getResponsiveSize(path!!, it.width, it.height)
                val lp = FrameLayout.LayoutParams(size.first, size.second)
                lp.gravity = Gravity.CENTER
                previewImage_image.layoutParams = lp
                previewImage_image.loadFromUrl(path, false)
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


    override fun onDestroy() {
        super.onDestroy()
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
