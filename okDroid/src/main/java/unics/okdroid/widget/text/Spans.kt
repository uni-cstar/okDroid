@file:JvmName("SpansKt")

package unics.okdroid.widget.text

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ReplacementSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px

/**
 * 高亮显示
 * @param start 渲染开始位置
 * @param end 渲染结束位置
 * @param color 颜色
 */
fun CharSequence.toHighLight(start: Int, end: Int, @ColorInt color: Int): SpannableStringBuilder {
    require(start >= 0 && end >= 0 && end >= start) {
        "start and end must be >= 0,and end must be greater than start."
    }
    val style = SpannableStringBuilder(this)
    style.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return style
}

/**
 * 高亮显示
 * @param tag 需要高亮的部分
 * @param color 渲染颜色
 * @return
 */
@Deprecated(message = "", replaceWith = ReplaceWith("toHighLight(color, tag)"))
fun CharSequence.toHighLight(tag: String, @ColorInt color: Int): SpannableStringBuilder {
    return toHighLight(color, tag)
}

/**
 * 高亮显示文本
 * @param color 文本高亮颜色
 * @param tags 需要高亮的文字，支持多个
 */
fun CharSequence.toHighLight(@ColorInt color: Int, vararg tags: String): SpannableStringBuilder {
    if (tags.isEmpty() || this.isEmpty())
        return SpannableStringBuilder()
    val ssb = SpannableStringBuilder(this)
    tags.filter {
        it.isNotEmpty()
    }.forEach {
        val index = this.indexOf(it)
        if (index >= 0) {
            ssb.setSpan(
                ForegroundColorSpan(color),
                index,
                index + it.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    return ssb
}


/**
 * 转换成可点击的内容
 */
fun CharSequence.toClickSpan(
    color: Int,
    vararg spans: Pair<String, OnSpanClick>
): SpannableStringBuilder {
    val ssb: SpannableStringBuilder =
        if (this is SpannableStringBuilder) this else SpannableStringBuilder(this)
    spans.forEach { (span, onSpanClick) ->
        if (span.isNotEmpty()) {
            val spanIndex = this.indexOf(span)
            if (spanIndex >= 0) {
                ssb.setSpan(
                    ClickSpan(span, color, onSpanClick),
                    spanIndex,
                    spanIndex + span.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
    return ssb
}

/**
 * 转换成可点击的内容
 */
fun CharSequence.toClickSpan(
    vararg spans: Triple<String, Int, OnSpanClick>
): SpannableStringBuilder {
    val ssb: SpannableStringBuilder =
        if (this is SpannableStringBuilder) this else SpannableStringBuilder(this)
    spans.forEach {
        val span = it.first
        if (span.isNotEmpty()) {
            val spanIndex = this.indexOf(span)
            if (spanIndex >= 0) {
                ssb.setSpan(
                    ClickSpan(span, it.second, it.third),
                    spanIndex,
                    spanIndex + span.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
    return ssb
}

/**
 * 应用可点击文本
 * @param content 文本内容
 * @param color 可点击部分文本颜色
 * 突出显示的文本以及点击响应的事件处理
 */
fun TextView.applyClickSpan(
    content: String,
    @ColorInt color: Int,
    vararg spans: Pair<String, OnSpanClick>
) {
    enableClickSpan()
    //设置点击颜色
    highlightColor = Color.TRANSPARENT
    text = content.toClickSpan(color, *spans)
}


/**
 * 确保TextView能够响应Span点击
 */
fun TextView.enableClickSpan() {
    if (movementMethod != LinkMovementMethod.getInstance()) {
        movementMethod = LinkMovementMethod.getInstance()
    }
}

/**
 * Span点击回调
 */
typealias OnSpanClick = (String, View) -> Unit

/**
 * Created by Lucio on 2020/11/25.
 * @param content 文本内容
 * @param color click span文本颜色
 * @param onClick 点击回调
 */
class ClickSpan(val content: String, @ColorInt val color: Int, val onClick: OnSpanClick) :
    ClickableSpan() {

    override fun updateDrawState(ds: TextPaint) {
//        super.updateDrawState(ds)
        ds.color = color
        ds.isUnderlineText = false
//        // 清除阴影
//        ds.clearShadowLayer()
    }

    override fun onClick(widget: View) {
        onClick.invoke(content, widget)
    }
}


/**
 * 垂直居中绝对文字大小
 * @see AbsoluteSizeSpan 该Span不是垂直居中
 */
class AbsoluteSizeCenterVerticalSpan(
    @Px private val textSizePixel: Float,
    private val textColor: Int = -1
) : ReplacementSpan() {

    private val textPaint: TextPaint = TextPaint()
    private val fm = Paint.FontMetricsInt()

    private fun setupTextPaint(src: Paint): TextPaint {
        textPaint.set(src)
        if (textColor != 0) {
            textPaint.color = textColor
        }
        textPaint.textSize = textSizePixel
        return textPaint
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if (text.isNullOrEmpty())
            return 0
        val subText = text.subSequence(start, end)
        if (subText.isEmpty())
            return 0
        return setupTextPaint(paint).measureText(subText.toString()).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        if (text.isNullOrEmpty())
            return
        val subText = text.subSequence(start, end)
        if (subText.isEmpty())
            return
        val textPaint: Paint = setupTextPaint(paint)
        textPaint.getFontMetricsInt(fm)
        canvas.drawText(
            subText.toString(),
            x,
            (y - ((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2)).toFloat(),
            textPaint
        ) //此处重新计算y坐标，使字体居中
    }
}