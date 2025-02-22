package unics.okdroid.widget.dimension

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * 按比例显示的FrameLayout
 */
class DimensionRatioRelativeLayout
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr),
    DimensionRatioViewHelper.OnDimensionRatioInteraction {

    private val mHelper: DimensionRatioViewGroupHelper

    init {
        mHelper = DimensionRatioViewGroupHelper(this)
        mHelper.resolveAttributes(context, attrs, defStyleAttr)
    }

    fun setDimensionRatio(ratioWidth: Int, ratioHeight: Int): DimensionRatioRelativeLayout {
        mHelper.setDimensionRatio(ratioWidth, ratioHeight)
        return this
    }

    fun setMode(@AspectMode mode: Int): DimensionRatioRelativeLayout {
        mHelper.setMode(mode)
        return this
    }

    override fun drRequestLayout() {
        requestLayout()
    }

    override fun drSetMeasuredDimension(measuredWidth: Int, measuredHeight: Int) {
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    @SuppressLint("WrongCall")
    override fun drCallSuperOnMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mHelper.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}

