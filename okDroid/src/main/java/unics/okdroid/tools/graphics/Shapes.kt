package unics.okdroid.tools.graphics

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.RoundRectShape

/**
 * 圆角形状的Drawable
 * @param radius 四个角半径
 */
fun RoundedRectShapeDrawable(radius: Float): ShapeDrawable {
    return RoundedRectShapeDrawable(radius, radius, radius, radius)
}

/**
 * 圆角形状的Drawable
 * @param leftTop 左上角半径
 * @param rightTop 右上角半径
 * @param rightBottom 右下角半径
 * @param leftBottom 左下角半径
 */
fun RoundedRectShapeDrawable(
    leftTop: Float,
    rightTop: Float,
    rightBottom: Float,
    leftBottom: Float
): ShapeDrawable {
    return RoundedRectShapeDrawable(
        floatArrayOf(
            leftTop,
            leftTop,
            rightTop,
            rightTop,
            rightBottom,
            rightBottom,
            leftBottom,
            leftBottom
        )
    )
}

/**
 * @param radii 圆角参数
参数分别对应：从左上->右上->右下->左下方向，一个角对应两个参数，比如第一个参数为左上角左边圆弧半径，第二个参数为左上角上边圆弧半径
左上x2,右上x2,右下x2,左下x2，注意顺序（顺时针依次设置
 */
fun RoundedRectShapeDrawable(radii: FloatArray): ShapeDrawable {
    return ShapeDrawable(RoundRectShape(radii, null, null))
}

/**
 * 半圆弧四边形：左右两边为半圆弧
 * @param color 颜色值
 * @param alpha 透明度
 */
fun CircleArcShapeDrawable(
    color: Int,
    @androidx.annotation.IntRange(from = 0, to = 255) alpha: Int = 255
): ShapeDrawable {
    val rectShape = ShapeDrawable(CircleArcRectShape())
    rectShape.paint.apply {
        this.color = color
        isAntiAlias = true
        style = Paint.Style.FILL
        this.alpha = alpha
    }
    return rectShape
}

/**
 * 带状态的半圆弧四边形
 */
fun CircleArcStateListDrawable(defaultColor: Int, activeColor: Int): Drawable {
    val defaultDrawable = CircleArcShapeDrawable(defaultColor)
    val activeDrawable = CircleArcShapeDrawable(activeColor)
    val drawable = StateListDrawable()
    drawable.addState(intArrayOf(android.R.attr.state_pressed), activeDrawable)
    drawable.addState(intArrayOf(), defaultDrawable)
    return drawable
}