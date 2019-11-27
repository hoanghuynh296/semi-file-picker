package vn.semicolon.filepicker

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_preview_image.*
import vn.semicolon.filepicker.ImageUtils.getSize
import java.io.File
import java.io.FileNotFoundException


internal class PreviewImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)
        val path = intent.getStringExtra("data")
        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener((previewImage_image)))
        previewImage_container.post {
            previewImage_container.let {
                val size = getResponsiveSize(path, it.width, it.height)
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

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        mScaleGestureDetector?.onTouchEvent(motionEvent)
        return true
    }

    private var mScaleGestureDetector: ScaleGestureDetector? = null
    override fun onDestroy() {
        super.onDestroy()
        mScaleGestureDetector = null
    }

    private class ScaleListener(var img: ImageView, var scaleFactor: Float = 1.0f) :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            scaleFactor *= scaleGestureDetector.scaleFactor
            scaleFactor = Math.max(
                1.0f,
                Math.min(scaleFactor, 10.0f)
            )
            img.scaleX = scaleFactor
            img.scaleY = scaleFactor
            return true
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
