package unics.okdroid.tools.graphics

import android.graphics.Paint
import androidx.annotation.Px
import kotlin.math.ceil

/**
 * Created by Lucio on 2021/12/11.
 */

val Paint.baseline: Float
    get() {
        val fontMetrics = this.fontMetrics
        return (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    }

/**
 * 文字高度
 */
val Paint.fontHeight:Float get() {
    val fontMetrics = this.fontMetrics
    return (fontMetrics.bottom - fontMetrics.top)
}

/**
 * 文本垂直居中绘制的y坐标基础值（最终结果再加上控件高度的一半或者绘制区域高度的一半即可得到最终的y坐标）
 */
val Paint.textCenterVerticalBaseY: Float
    get() {
//        val fontMetrics = this.fontMetrics
//        return if (textAlign == Paint.Align.CENTER) {
//            (fontMetrics.top + fontMetrics.bottom) / 2
//        } else {
//            (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.descent
//        }
        //有些文章末尾减去的descent，有些是减去的bottom，总体感觉绘制中文的话减去bottom更合适
        return (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom// fontMetrics.descent
    }

/**
 * 文本垂直居中的y坐标
 * @param height 绘制区域高度：一般可以传View的高度
 */
fun Paint.textCenterVerticalY(height: Int): Float {
    return height / 2 + textCenterVerticalBaseY
}

/**
 * 获取文本高度
 */
@Px
fun Paint.getTextHeight(): Int {
    val fm = fontMetrics
    return ceil((fm.descent - fm.ascent).toDouble()).toInt()
}
