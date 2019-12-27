package vn.semicolon.filepicker

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vn.semicolon.base.widget.adapter.BaseAdapter
import kotlin.math.max
import kotlin.math.min

fun RecyclerView.setScrollable(scrollable: Boolean) {
    if (layoutManager is ScrollGridLayoutManager) {
        (layoutManager as ScrollGridLayoutManager).scrollable = scrollable
    } else throw Exception("This function only support ScrollGridLayoutManager")
}

class ScrollGridLayoutManager : GridLayoutManager {
    var scrollable: Boolean = true

    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, spanCount: Int) : super(context, spanCount)


    constructor(
        context: Context, spanCount: Int,
        @RecyclerView.Orientation orientation: Int, reverseLayout: Boolean
    ) : super(context, spanCount, orientation, reverseLayout)

    override fun canScrollHorizontally(): Boolean {
        return if (!scrollable && super.canScrollHorizontally())
            scrollable else super.canScrollHorizontally()
    }

    override fun canScrollVertically(): Boolean {
        return if (!scrollable && super.canScrollVertically())
            scrollable else super.canScrollVertically()
    }
}

abstract class SelectableAdapter<O> : BaseAdapter<O>() {
    protected val mSelectedItems: ArrayList<O> = ArrayList()
    var minSelect: Int = 1
    var maxSelect: Int = 1
    protected open fun onOverMaxError(max: Int) {}
    protected open fun onBelowMinError(currentCount: Int, min: Int) {}
    protected open fun select(index: Int) {}
    protected open fun select(item: O) {}
    protected open fun unSelect(index: Int) {}
    protected open fun unSelect(item: O) {}
}

abstract class SelectOnHoverAdapter<O> : SelectableAdapter<O>() {
    private var lastTouchPoint: PointF? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        setupTouchHandle(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    private var selectOnHover = false
    protected var recyclerView: RecyclerView? = null
    private fun getItemByLocation(rv: RecyclerView, x: Float, y: Float): O? {
        getItemIndexByLocation(rv, x, y).let {
            if (it == -1) return null
            return getItemAt(it)
        }
    }

    private fun getItemIndexByLocation(rv: RecyclerView, x: Float, y: Float): Int {
        rv.findChildViewUnder(x, y)?.let {
            (rv.layoutManager as? LinearLayoutManager)?.getPosition(it)?.let { pos ->
                return pos
            }
        }
        return -1
    }

    private fun setSelectOnHoverEnable(rv: RecyclerView, isEnable: Boolean) {
        selectOnHover = isEnable
        hoverInfo.reset()
        rv.setScrollable(!isEnable)
    }

    protected fun isItemSelected(item: O) = mSelectedItems.contains(item)
    private val hoverInfo = HoverInfo()

    private fun setupTouchHandle(rv: RecyclerView) {
        val touchTracker = TouchMoveTracker { direction, x, y ->
            Log.d("xxx", "direction: $direction")
            hoverInfo.lastLocation.x = x
            hoverInfo.lastLocation.y = y

            if (!selectOnHover)
                when (direction) {
                    TouchMoveTracker.Direction.LEFT, TouchMoveTracker.Direction.RIGHT -> {
                        setSelectOnHoverEnable(rv, true)
                        getItemIndexByLocation(rv, x, y).let { index ->
                            Log.d("FileAdapter", "first hover on $index")
                            hoverInfo.let {
                                it.startIndex = index
                                it.lastIndex = index
                                getItemAt(index)?.let { item ->
                                    it.isSelecting = !isItemSelected(item)
                                }
                            }
                        }
                    }
                }
            else {
                val hoveredIndex = getItemIndexByLocation(rv, x, y)
                Log.d("FileAdapter", "second hover on $hoveredIndex")
                if (hoveredIndex != hoverInfo.lastIndex && hoveredIndex != -1) {
                    if (hoverInfo.isSelecting) {
                        selectRange(
                            min(hoverInfo.lastIndex, hoveredIndex),
                            max(hoverInfo.lastIndex, hoveredIndex)
                        )
                    } else
                        unSelectRange(
                            min(hoverInfo.lastIndex, hoveredIndex),
                            max(hoverInfo.lastIndex, hoveredIndex)
                        )
                    hoverInfo.lastIndex = hoveredIndex
                }
            }
        }

        rv.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (maxSelect == 1) return false
                touchTracker.onEvent(rv, e)
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        lastTouchPoint = PointF()
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        Log.d("FileAdapter", "cancel hover select ----------->")
                        setSelectOnHoverEnable(rv, false)
                        lastTouchPoint = null
                    }
                    MotionEvent.ACTION_MOVE -> {
                        lastTouchPoint!!.apply {
                            x = e.x
                            y = e.y
                        }
                        Log.d("iii", "move on: ${e.x} ${e.y}, rv height: ${rv.height}")
                        onTouchMove(lastTouchPoint)
                    }
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }

        })
    }

    private fun canScrollUp(): Boolean {
        (recyclerView?.layoutManager as? ScrollGridLayoutManager)?.let {
            Log.d("mmm", "first visible: ${it.findFirstCompletelyVisibleItemPosition()}")
            return it.findFirstCompletelyVisibleItemPosition() > it.spanCount
        }
        return false
    }

    private fun canScrollDown(): Boolean {
        (recyclerView?.layoutManager as? ScrollGridLayoutManager)?.let {
            Log.d("mmm", "last visible: ${it.findLastCompletelyVisibleItemPosition()}")
            return it.findLastCompletelyVisibleItemPosition() < (itemCount - it.spanCount)
        }
        return false
    }

    private fun onTouchMove(lastPoint: PointF?) {

        if (lastPoint == null || recyclerView == null || !selectOnHover) return
        val rv = recyclerView!!
        Log.d(
            "iii",
            "detect: ${lastPoint?.x} ${lastPoint?.y}, rv height: ${rv.height},selectOnHover: $selectOnHover, canScrollUD: ${canScrollUp()} ${canScrollDown()}"
        )
        when {
            lastPoint.y <= 30 && canScrollUp() -> {
                Log.d(
                    "iii", "scroll up ---------------__>"
                )
                rv.setScrollable(true)
                rv.scrollBy(0, -1)
                rv.post {
                    onTouchMove(lastTouchPoint)
                }
            }
            (rv.height - lastPoint.y <= 30 || rv.height < lastPoint.y) && canScrollDown() -> {
                rv.setScrollable(true)
                rv.scrollBy(0, 1)
                rv.post {
                    onTouchMove(lastTouchPoint)
                }
                Log.d(
                    "iii", "scroll down ---------------__>"
                )
            }
        }
    }

    override fun select(index: Int) {
        selectRange(index, index)
    }

    override fun unSelect(index: Int) {
        unSelectRange(index, index)
    }

    protected open fun selectRange(from: Int, end: Int) {
        Log.d("FileAdapter", "selectRange($from,$end)")
        doSelectRange(from, end)
        onSelectChanged(min(from, end), max(from, end), mSelectedItems.size)
    }

    protected open fun unSelectRange(from: Int, end: Int) {
        Log.d("FileAdapter", "unSelectRange($from,$end)")
        doUnSelectRange(from, end)
        onSelectChanged(min(from, end), max(from, end), mSelectedItems.size)
    }

    protected open fun onSelectChanged(from: Int, end: Int, selectedCount: Int) {

    }

    private fun doUnSelectRange(from: Int, end: Int) {
        for (i in from..end) {
            getItemAt(i)?.let {
                doUnSelect(it)
            }
        }
    }

    private fun doSelectRange(from: Int, end: Int) {
        for (i in from..end) {
            getItemAt(i)?.let {
                doSelect(it)
            }
        }
    }

    private fun doSelect(item: O) {
        if (maxSelect == 1) { // single choice
            mSelectedItems.clear()
            mSelectedItems.add(item)
        } else if (maxSelect > 1 && isFullSelect()) {
            onOverMaxError(maxSelect)
        } else {
            if (!mSelectedItems.contains(item))
                mSelectedItems.add(item)
        }
    }

    protected fun isFullSelect(): Boolean {
        return mSelectedItems.size == maxSelect
    }


    private fun doUnSelect(item: O) {
        if (mSelectedItems.contains(item))
            mSelectedItems.remove(item)
    }

    override fun select(item: O) {
        if (!mSelectedItems.contains(item)) {
            doSelect(item)
            val index = data.indexOf(item)
            onSelectChanged(index, index, mSelectedItems.size)
        }
    }

    override fun unSelect(item: O) {
        if (mSelectedItems.contains(item)) {
            doUnSelect(item)
            val index = data.indexOf(item)
            onSelectChanged(index, index, mSelectedItems.size)
        }
    }
}

private data class HoverInfo(
    var startIndex: Int = -1,
    var lastIndex: Int = -1,
    var lastLocation: PointF = PointF(),
    var isSelecting: Boolean = false
) {
    fun reset() {
        startIndex = -1
        lastIndex = -1
        lastLocation.x = -1f
        lastLocation.y = -1f
        isSelecting = false
    }
}
