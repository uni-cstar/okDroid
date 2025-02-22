package unics.okdroid.tools.ui

import android.app.Activity
import android.app.Application
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import unics.okdroid.widget.GrayFrameLayout

/**
 * 使用灰色主题："哀悼主题"
 * @see [Application.registerActivityLifecycleCallbacks]可以结合该方法对整个程序应用灰色主题
 * @see [GrayFrameLayout] 可以使用该布局实现布局内的view使用灰色主题
 * @see [View.applyGrayTheme]
 * @see [View.clearGrayTheme]
 */
fun Activity.applyGrayTheme() {
    window.decorView.applyGrayTheme()
}

fun Activity.clearGrayTheme() {
    window.decorView.clearGrayTheme()
}

/**
 * 控件使用灰色主题
 */
fun View.applyGrayTheme() {
    //todo 据说开启硬件加速，对某些直播场景会有影响
    //    window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

    //使用soft方式会让界面内存消耗变多，界面在低端盒子上变卡
    setLayerType(View.LAYER_TYPE_SOFTWARE, grayPaint())
}

/**
 * 控件取消灰色主题
 * 注意：如果其parent使用了灰色主题（滤镜），那么child取消灰色主题也没有作用：因为其parent所在的整个canvas已经做了滤镜处理了
 */
fun View.clearGrayTheme() {
    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
}

private fun grayPaint() = saturationPaint(0f)

/**
 * 饱和度画笔
 */
private fun saturationPaint(saturation: Float): Paint {
    val paint = Paint()
    val cm = ColorMatrix()
    //ColorMatrix中setSaturation设置饱和度，给布局去色(0为灰色，1为原图)
    cm.setSaturation(saturation)//灰度效果
    paint.colorFilter = ColorMatrixColorFilter(cm)
    return paint
}

