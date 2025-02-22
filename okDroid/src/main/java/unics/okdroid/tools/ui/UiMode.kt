package unics.okdroid.tools.ui

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.IntDef

/**
 * Android每秒满帧为60帧（现在也有120帧的设备了）
 */
const val FULL_PER_SECOND_FRAMES = 60

/**
 * 一帧的时间，大致为16ms
 */
const val ONE_FRAME_TIME = 1000 / FULL_PER_SECOND_FRAMES

const val UI_MODE_TYPE_UNDEFINED = Configuration.UI_MODE_TYPE_UNDEFINED
const val UI_MODE_TYPE_NORMAL = Configuration.UI_MODE_TYPE_NORMAL
const val UI_MODE_TYPE_TELEVISION = Configuration.UI_MODE_TYPE_TELEVISION
const val UI_MODE_TYPE_CAR = Configuration.UI_MODE_TYPE_CAR

@Retention(AnnotationRetention.SOURCE)
@IntDef(UI_MODE_TYPE_UNDEFINED, UI_MODE_TYPE_NORMAL, UI_MODE_TYPE_TELEVISION, UI_MODE_TYPE_CAR)
annotation class UiModeType

@UiModeType
var currentUiModeType: Int = UI_MODE_TYPE_UNDEFINED
    internal set

/**
 * 当前是否运行在TV上，这是一个官方给出的判断方式，但是在国内的环境下，并不可取。因为大部分厂商的智能电视，只是拿普通的 Android 系统改了改，其实并没有遵循 Google TV 的标准，所以这种方式在某些设备上可能会判断出错。
 * @note :在某些TV设备上 读取到的值为false,[UiModeManager.getCurrentModeType] = [Configuration.UI_MODE_TYPE_NORMAL]
 *
 */
internal inline fun Context.isTelevisionUiModeSys(): Boolean {
    val uiModeManager =
        this.applicationContext.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            ?: return false
    return (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
}

/**
 * 当前是否是TV UI 模式
 */
fun Context.isTelevisionUiMode(): Boolean {
    return currentUiModeType == UI_MODE_TYPE_TELEVISION || isTelevisionUiModeSys()
}

/**
 *
 * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
 * @note 该方法通过判断是设备尺寸，也可能不准
 * @return 平板返回 True，手机返回 False
 */
fun Context.isTablet(): Boolean {
    return resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
}
