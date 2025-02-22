@file:JvmName("AnimationsKt")
@file:JvmMultifileClass

package unics.okdroid.tools.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import unics.okcore.lang.annotation.Note

/**
 * 推荐场景：
 * - 适合垂直方向做指示动作的动画：比如向上或向下的箭头
 * - 比如播放器长按的箭头指示动作
 *
 * 该动画的本质是在Y轴做轻微的平移 + 渐隐 循环动画
 *
 * 注意：该动画是循环永久执行的动画，需要在手动不再使用的时候，移除动画
 */
@Note(message = "注意：该动画是循环永久执行的动画，需要在不再使用的时候，手动移除动画")
fun VerticalNavigationAnimator(
    target: View,
    yOffset: Float = 5f,
    alphaStart: Float = 0.6f,
    alphaEnd: Float = 1.0f,
): Animator {
    val animator = ObjectAnimator.ofPropertyValuesHolder(
        target,
        PropertyValuesHolder.ofFloat("translationY", 0f, yOffset),
        PropertyValuesHolder.ofFloat("alpha", alphaStart, alphaEnd)
    )
    animator.duration = 1500
    animator.repeatMode = ValueAnimator.REVERSE
    animator.repeatCount = ValueAnimator.INFINITE
    animator.interpolator = LinearInterpolator()
    return animator
}