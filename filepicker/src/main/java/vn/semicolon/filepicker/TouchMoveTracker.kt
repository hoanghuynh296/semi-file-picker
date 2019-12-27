package vn.semicolon.filepicker

import android.graphics.Point
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class TouchMoveTracker(var callback: (moveDirection: Int, x: Float, y: Float) -> Unit) {
    companion object {
        private const val ACCEPT_DIFFERENCE = 5
        private const val ACCEPT_MOVE = 10
    }

    object Direction {
        const val UP = 0
        const val DOWN = 1
        const val LEFT = 2
        const val RIGHT = 3
        const val NO_DEFINE = -1
    }

    private val ACCURACY_COUNT = 3
    private var lastPosition: Point? = null
    private val movePoints = ArrayList<PointF>()
    private fun resetTracking() {
//        lastPosition = null
        movePoints.clear()
    }

    private fun getMoveDirection(vararg point: PointF): Int {
        val x = point.last().x - point.first().x
        val y = point.last().y - point.first().y

        when {
            x > 0 && abs(x) >= ACCEPT_MOVE && abs(y) < ACCEPT_DIFFERENCE -> {
                Log.d("TouchMoveTracker", "x: $x y: $y -> RIGHT")
                return Direction.RIGHT
            }
            x < 0 && abs(x) >= ACCEPT_MOVE && abs(y) < ACCEPT_DIFFERENCE -> {
                Log.d("TouchMoveTracker", "x: $x y: $y -> LEFT")
                return Direction.LEFT
            }
            y > 0 && abs(y) >= ACCEPT_MOVE && abs(x) < ACCEPT_DIFFERENCE -> {
                Log.d("TouchMoveTracker", "x: $x y: $y -> DOWN")
                return Direction.DOWN
            }
            y < 0 && abs(y) >= ACCEPT_MOVE && abs(x) < ACCEPT_DIFFERENCE -> {
                Log.d("TouchMoveTracker", "x: $x y: $y -> UP")
                return Direction.UP
            }
        }
        return Direction.NO_DEFINE
    }

    fun onEvent(v: View?, e: MotionEvent?) {
        when (e?.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                resetTracking()
            }
            MotionEvent.ACTION_MOVE -> {
                if (movePoints.size == ACCURACY_COUNT) {
                    callback.invoke(
                        getMoveDirection(
                            *movePoints.toTypedArray()
                        ), e.x, e.y
                    )
                    movePoints.removeAt(0)
                }
                movePoints.add(PointF(e.x, e.y))
            }
        }
    }
}