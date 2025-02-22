@file:JvmName("ResourcesKt")
@file:JvmMultifileClass

/**
 * Created by Lucio on 2022/3/4.
 */

package unics.okdroid.tools.res

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.IntDef
import androidx.annotation.Px
import androidx.fragment.app.Fragment
import unics.okdroid.globalContext

/**
 * 注入的上下文
 */
inline val ctxInject: Context get() = globalContext

inline val Context.density get() = resources.displayMetrics.density
inline val Context.scaledDensity get() = resources.displayMetrics.scaledDensity

@IntDef(
    value = [TypedValue.COMPLEX_UNIT_PX,
        TypedValue.COMPLEX_UNIT_DIP,
        TypedValue.COMPLEX_UNIT_SP,
        TypedValue.COMPLEX_UNIT_PT,
        TypedValue.COMPLEX_UNIT_IN,
        TypedValue.COMPLEX_UNIT_MM]
)
@Retention(AnnotationRetention.SOURCE)
annotation class ComplexDimensionUnit

inline fun Context.unitValue(@ComplexDimensionUnit unit: Int, value: Float): Float =
    TypedValue.applyDimension(unit, value, this.resources.displayMetrics)


//扩展Number类型（依赖库注入的Context）
inline val Int.dp: Float get() = toFloat().dp
inline val Int.dpInt: Int get() = toFloat().dpInt
inline val Double.dp: Float get() = toFloat().dp
inline val Double.dpInt: Int get() = toFloat().dpInt
inline val Int.sp: Float get() = toFloat().sp
inline val Int.spInt: Int get() = toFloat().spInt
inline val Double.sp: Float get() = toFloat().sp
inline val Double.spInt: Int get() = toFloat().spInt

//Number扩展的基础方法（依赖库注入的Context）
inline val Float.dp: Float get() = ctxInject.dp(this)
inline val Float.dpInt: Int get() = ctxInject.dpInt(this)
inline val Float.sp: Float get() = ctxInject.sp(this)
inline val Float.spInt: Int get() = ctxInject.spInt(this)

inline fun Context.dp(value: Float): Float = unitValue(TypedValue.COMPLEX_UNIT_DIP, value)
inline fun Context.dpInt(value: Float): Int = dp(value).toInt()
inline fun Context.dp(value: Int): Float = dp(value.toFloat())
inline fun Context.dpInt(value: Int): Int = dpInt(value.toFloat())
inline fun Context.sp(value: Float): Float = unitValue(TypedValue.COMPLEX_UNIT_SP, value)
inline fun Context.spInt(value: Float): Int = sp(value).toInt()
inline fun Context.sp(value: Int): Float = sp(value.toFloat())
inline fun Context.spInt(value: Int): Int = spInt(value.toFloat())
inline fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)
inline fun Context.px2dp(px: Int): Float = px.toFloat() / density
inline fun Context.px2sp(px: Int): Float = px.toFloat() / resources.displayMetrics.scaledDensity


inline fun View.dp(value: Float): Float = context.dp(value)
inline fun View.dpInt(value: Float): Int = context.dpInt(value)
inline fun View.dp(value: Int): Float = dp(value.toFloat())
inline fun View.dpInt(value: Int): Int = dpInt(value.toFloat())
inline fun View.sp(value: Float): Float = context.sp(value)
inline fun View.spInt(value: Float): Int = context.spInt(value)
inline fun View.sp(value: Int): Float = sp(value.toFloat())
inline fun View.spInt(value: Int): Int = spInt(value.toFloat())
inline fun View.dimen(@DimenRes resource: Int): Int = context.dimen(resource)
inline fun View.px2dip(@Px px: Int): Float = context.px2dp(px)
inline fun View.px2sp(@Px px: Int): Float = context.px2sp(px)

inline fun Fragment.dp(value: Float): Float = requireContext().dp(value)
inline fun Fragment.dpInt(value: Float): Int = requireContext().dpInt(value)
inline fun Fragment.dp(value: Int): Float = dp(value.toFloat())
inline fun Fragment.dpInt(value: Int): Int = dpInt(value.toFloat())
inline fun Fragment.sp(value: Float): Float = requireContext().sp(value)
inline fun Fragment.spInt(value: Float): Int = requireContext().spInt(value)
inline fun Fragment.sp(value: Int): Float = sp(value.toFloat())
inline fun Fragment.spInt(value: Int): Int = spInt(value.toFloat())
inline fun Fragment.dimen(@DimenRes resource: Int): Int = requireContext().dimen(resource)





