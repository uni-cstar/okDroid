@file:JvmName("AnimationsKt")
@file:JvmMultifileClass

package unics.okdroid.tools.animation

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import unics.okdroid.R
import unics.okdroid.deprecated.log.logd

/**
 * 动画调整view的大小
 * @param targetWidth 最终宽度
 * @param targetHeight 最终高度
 * @param reuseAnim 是否重用Animator,即如果之前已经运行过resizeAnimation，则重复使用该对象
 * @return 动画对象，当期望的宽和高与控件的宽和高一致时，会返回null
 */
fun View.startResize(
    targetWidth: Int,
    targetHeight: Int,
    reuseAnim: Boolean = true,
    expectCancelCurrent: Boolean = true,
    onAnimatorCreate: ((Animator) -> Unit)? = null
): Animator {
    return startResize(
        fromWidth = this.width,
        toWidth = targetWidth,
        fromHeight = this.height,
        toHeight = targetHeight,
        reuseAnim = reuseAnim,
        expectCancelCurrent = expectCancelCurrent,
        onAnimatorCreate = onAnimatorCreate
    )
}

/**
 * 大小调整动画，宽度从[fromWidth]调整至[toWidth]，高度从[fromHeight]调整[toHeight]
 * @param fromWidth 初始宽度
 * @param toWidth 最终宽度
 * @param fromHeight 初始高度
 * @param toHeight 最终高度
 * @param reuseAnim 是否重用Animator,即如果之前已经运行过resizeAnimation，则重复使用该对象
 * @param expectCancelCurrent 是否取消当前存在的同类动画，默认是true，一般不用改该参数
 * @param onAnimatorCreate 初始动画创建的时候会回调该方法：作用是预留对外提供动画的一些设置，比如动画时间，动画插值器等
 */
fun View.startResize(
    fromWidth: Int,
    toWidth: Int,
    fromHeight: Int,
    toHeight: Int,
    reuseAnim: Boolean = true,
    expectCancelCurrent: Boolean = true,
    onAnimatorCreate: ((Animator) -> Unit)? = null
): Animator {
//    if (this.width == toWidth && this.height == toHeight) {//如果大小已经与目标大小相同，直接设置布局即可
//        this.updateLayoutSize(toWidth, toHeight)
//        return null
//    }
    var animator = getTag(R.id.ucs_anim_tag_resize) as? ResizeAnimator
    if (expectCancelCurrent) {
        animator?.cancel()
    }
    if (!reuseAnim)
        animator = null
    val anim = animator?.apply {
        logd("ResizeAnim") { "reuse resize width animation,set width" }
        this.fromWidth = fromWidth
        this.toWidth = toWidth
        this.fromHeight = fromHeight
        this.toHeight = toHeight
    } ?: kotlin.run {
        logd("ResizeAnim") { "create resize width animation." }
        ResizeAnimator(
            this,
            fromWidth = fromWidth,
            toWidth = toWidth,
            fromHeight = fromHeight,
            toHeight = toHeight
        ).also { animator ->
            onAnimatorCreate?.invoke(animator)
            setTag(R.id.ucs_anim_tag_resize, animator)
        }
    }
    anim.start()
    return anim
}

/**
 * 宽度动画（从当前宽度）调整至指定宽度
 * @param toWidth 最终宽度
 * @param reuseAnim:Boolean = true,
 * @param expectCancelCurrent 是否取消当前存在的同类动画，默认是true，一般不用改该参数
 * @param onAnimatorCreate 初始动画创建的时候会回调该方法：作用是预留对外提供动画的一些设置，比如动画时间，动画插值器等
 */
fun View.startResizeWidth(
    toWidth: Int,
    reuseAnim: Boolean = true,
    expectCancelCurrent: Boolean = true,
    onAnimatorCreate: ((Animator) -> Unit)? = null
): Animator {
    return startResizeWidth(this.width, toWidth, reuseAnim, expectCancelCurrent, onAnimatorCreate)
}

/**
 * 宽度动画从[fromWidth]调整至[toWidth]
 * @param fromWidth 初始宽度
 * @param toWidth 最终宽度
 * @param reuseAnim 是否重用Animator,即如果之前已经运行过resizeAnimation，则重复使用该对象
 * @param expectCancelCurrent 是否取消当前存在的同类动画，默认是true，一般不用改该参数
 * @param onAnimatorCreate 初始动画创建的时候会回调该方法：作用是预留对外提供动画的一些设置，比如动画时间，动画插值器等
 */
fun View.startResizeWidth(
    fromWidth: Int,
    toWidth: Int,
    reuseAnim: Boolean = true,
    expectCancelCurrent: Boolean = true,
    onAnimatorCreate: ((Animator) -> Unit)? = null
): Animator {
//    if (this.width == toWidth) {//如果大小已经与目标大小相同，直接设置布局即可
//        this.updateLayoutWidth(toWidth)
//        return
//    }
    var animator = getTag(R.id.ucs_anim_tag_resize_width) as? ResizeAnimator
    if (expectCancelCurrent) {
        animator?.cancel()
    }
    if (!reuseAnim)
        animator = null
    val anim = animator?.apply {
        logd("ResizeAnim") { "reuse resize width animation,set width" }
        this.fromWidth = fromWidth
        this.toWidth = toWidth
    } ?: kotlin.run {
        logd("ResizeAnim") { "create resize width animation." }
        ResizeWidthAnimator(this, fromWidth = fromWidth, toWidth = toWidth).also { animator ->
            onAnimatorCreate?.invoke(animator)
            setTag(R.id.ucs_anim_tag_resize_width, animator)
        }
    }
    anim.start()
    return anim
}


/**
 * 动画（从当前高度）调整至指定高度
 * @param toHeight 最终高度
 * @param expectCancelCurrent 是否取消当前存在的同类动画，默认是true，一般不用改该参数
 * @param onAnimatorCreate 初始动画创建的时候会回调该方法：作用是预留对外提供动画的一些设置，比如动画时间，动画插值器等
 */
fun View.startResizeHeight(
    toHeight: Int,
    expectCancelCurrent: Boolean = true,
    onAnimatorCreate: ((Animator) -> Unit)? = null
): Animator {
    return startResizeHeight(
        fromHeight = this.height,
        toHeight = toHeight,
        expectCancelCurrent = expectCancelCurrent,
        onAnimatorCreate = onAnimatorCreate
    )
}

/**
 * 高度动画从[fromHeight]调整至[toHeight]
 * @param fromHeight 初始高度
 * @param toHeight 最终高度
 * @param expectCancelCurrent 是否取消当前存在的同类动画，默认是true，一般不用改该参数
 * @param onAnimatorCreate 初始动画创建的时候会回调该方法：作用是预留对外提供动画的一些设置，比如动画时间，动画插值器等
 */
fun View.startResizeHeight(
    fromHeight: Int,
    toHeight: Int,
    expectCancelCurrent: Boolean = true,
    onAnimatorCreate: ((Animator) -> Unit)? = null
): Animator {
//    if (this.height == toHeight) {//如果大小已经与目标大小相同，直接设置布局即可
//        this.updateLayoutHeight(toHeight)
//        return
//    }
    val animator = getTag(R.id.ucs_anim_tag_resize_height) as? ResizeAnimator
    if (expectCancelCurrent) {
        animator?.cancel()
    }
    val anim = animator?.apply {
        logd("ResizeAnim") { "reuse resize height animation,set width" }
        this.fromHeight = fromHeight
        this.toHeight = toHeight
    } ?: kotlin.run {
        logd("ResizeAnim") { "create resize height animation." }
        ResizeHeightAnimator(this, fromHeight = fromHeight, toHeight = toHeight).also { animator ->
            onAnimatorCreate?.invoke(animator)
            setTag(R.id.ucs_anim_tag_resize_height, animator)
        }
    }
    anim.start()
    return anim
}

/**
 * 调整宽度动画
 * @param toWidth 目标宽度
 */
inline fun ResizeWidthAnimator(
    target: View,
    toWidth: Int
): ValueAnimator {
    return ResizeWidthAnimator(target, fromWidth = target.width, toWidth = toWidth)
}

/**
 * 调整宽度动画
 * @param fromWidth 起始宽度
 * @param toWidth 目标宽度
 */
inline fun ResizeWidthAnimator(
    target: View,
    fromWidth: Int,
    toWidth: Int
): ValueAnimator {
    return ResizeAnimator(
        target,
        fromWidth,
        toWidth,
        target.height,
        target.height
    ).also { animator ->
        animator.type = RESIZE_TYPE_WIDTH
    }
}

/**
 * 调整高度动画
 * @param toHeight 最终高度
 */
inline fun ResizeHeightAnimator(
    target: View,
    toHeight: Int
): ValueAnimator {
    return ResizeHeightAnimator(target, fromHeight = target.height, toHeight = toHeight)
}

/**
 * 调整高度动画
 * @param fromHeight 起始高度
 * @param toHeight 目标高度
 */
inline fun ResizeHeightAnimator(
    target: View,
    fromHeight: Int,
    toHeight: Int
): ValueAnimator {
    return ResizeAnimator(
        target,
        target.width,
        target.width,
        fromHeight,
        toHeight
    ).also { animator ->
        animator.type = RESIZE_TYPE_HEIGHT
    }
}

/**
 * 调整宽高动画
 * @param fromWidth 起始宽度
 * @param toWidth 目标宽度
 * @param fromHeight 起始高度
 * @param toHeight 目标高度
 */
class ResizeAnimator(
    @JvmField
    var target: View,
    @JvmField
    var fromWidth: Int,
    @JvmField
    var toWidth: Int,
    @JvmField
    var fromHeight: Int,
    @JvmField
    var toHeight: Int
) : ValueAnimator() {

    @PublishedApi
    @JvmField
    internal var type: Int = RESIZE_TYPE_SIZE

    init {
        //默认动画时间
        duration = 150
        setFloatValues(0f, 1f)
        addUpdateListener {
            val factory = it.animatedValue as Float
            target.layoutParams.apply {
                when (type) {
                    RESIZE_TYPE_WIDTH -> {
                        val newVal = (fromWidth + factory * (toWidth - fromWidth)).toInt()
                        if (newVal != width) {
                            width = newVal
                            target.requestLayout()
                        }
                    }

                    RESIZE_TYPE_HEIGHT -> {
                        val newVal = ((fromHeight + factory * (toHeight - fromHeight)).toInt())
                        if (newVal != height) {
                            height = newVal
                            target.requestLayout()
                        }
                    }

                    else -> {
                        val newW = (fromWidth + factory * (toWidth - fromWidth)).toInt()
                        val newH = ((fromHeight + factory * (toHeight - fromHeight)).toInt())
                        if (newW != width || newH != height) {
                            width = newW
                            height = newH
                            target.requestLayout()
                        }
                    }
                }
            }
        }
    }
}

@PublishedApi
internal const val RESIZE_TYPE_SIZE = 0

@PublishedApi
internal const val RESIZE_TYPE_WIDTH = 1

@PublishedApi
internal const val RESIZE_TYPE_HEIGHT = 2
