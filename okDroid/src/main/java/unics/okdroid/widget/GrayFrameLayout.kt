package unics.okdroid.widget

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 * 灰色主题的布局：即该布局下的控件显示都是灰色的
 * @see [bas.droid.core.app.applyGrayTheme] 整个[Activity]设置灰色主题
 */
class GrayFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val mPaint = Paint()

    init {
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        mPaint.colorFilter = ColorMatrixColorFilter(cm)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.saveLayer(null, mPaint, Canvas.ALL_SAVE_FLAG)
        super.onDraw(canvas)
        canvas?.restore()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.saveLayer(null, mPaint, Canvas.ALL_SAVE_FLAG)
        super.dispatchDraw(canvas)
        canvas?.restore()
    }

}