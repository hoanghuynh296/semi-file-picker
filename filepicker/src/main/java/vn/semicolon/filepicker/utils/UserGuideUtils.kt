package vn.semicolon.filepicker.utils

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import vn.semicolon.filepicker.R

object UserGuideUtils {
    fun show(root: FrameLayout, v: View, duration: Long = 3000, startDelay: Long = 300) {
        val lp = v.layoutParams ?: FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        v.layoutParams = lp
        v.alpha = 0f
        root.addView(v)
        root.post {
            v.animate()
                .alpha(1f)
                .setDuration(300)
                .withEndAction {
                    v.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            root.removeView(v)
                        }
                        .setStartDelay(duration)
                        .start()
                }
                .setStartDelay(startDelay)
                .start()
        }
    }

    private fun inflateLayout(c: Context, layoutId: Int): View {
        return LayoutInflater.from(c).inflate(layoutId, null)
    }

    fun showSwipeToSelect(activity: Activity) {
        val root = activity.findViewById<FrameLayout>(android.R.id.content)
        val view = inflateLayout(activity, R.layout.layout_user_guide_swipe_to_select)
        show(root, view)
    }
}