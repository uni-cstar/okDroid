/**
 * Created by Lucio on 2021/11/4.
 */
package unics.okdroid.kit.imageloader

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import halo.android.permission.BuildConfig
import unics.okdroid.R
import unics.okdroid.kit.imageloader.glide.GlideEngine

internal const val TAG = "OkImageLoader"

/**
 * 用于建议的默认disk cache 目录名字
 */
const val DEFAULT_DISK_CACHE_FOLDER_NAME = "bas_image_loader_disk_cache"

/**
 * 用于建议的默认disk cache 大小
 */
const val DEFAULT_DISK_CACHE_SIZE = 250 * 1024 * 1024L

/**
 * 初始化图片加载器
 * @param debuggable 是否开启调试模式
 * @param config 通用配置信息
 * @param engine 加载核心实现
 */
@JvmOverloads
fun initImageLoader(
    ctx: Context,
    debuggable: Boolean = unics.okdroid.kit.imageloader.debuggable,
    configs: Configs = unics.okdroid.kit.imageloader.configs,
    engine: Engine = unics.okdroid.kit.imageloader.engine
) {
    defaultImageCornerSizeAIL =
    ctx.applicationContext.resources.getDimensionPixelSize(R.dimen.ucs_image_corner_radius)
    unics.okdroid.kit.imageloader.debuggable = debuggable
    if (unics.okdroid.kit.imageloader.debuggable) {
        logi("init: defaultImageRoundingRadius=$defaultImageCornerSizeAIL")
    }
    unics.okdroid.kit.imageloader.configs = configs
    unics.okdroid.kit.imageloader.engine = engine
}

/**
 * 开启调试信息
 */
var debuggable: Boolean = BuildConfig.DEBUG

/**
 * 图片加载器引擎：默认采用Glide实现图片加载
 */
var engine: Engine = GlideEngine

/**
 * 加载器配置
 */
var configs: Configs = Configs()

/**
 * 默认图片圆角半径
 * [Configs]中未提供该属性，主要是考虑通过代码设置的值无法写入默认的圆角图片资源（shape xml）中，
 * 所以修改该值可以通过提供 资源<dimen name="image_corner_radius_ail"></dimen>覆盖。
 */
var defaultImageCornerSizeAIL: Int = 12

/**
 * 默认图片资源
 */
@get:DrawableRes
val defaultImageResAIL: Int
    get() = configs.defaultImageRes

/**
 * 默认圆角图片
 */
@get:DrawableRes
val defaultRoundedImageResAIL: Int
    get() = configs.defaultRoundedImageRes

/**
 * 默认圆形图片
 */
@get:DrawableRes
val defaultCircleImageResAIL: Int
    get() = configs.defaultCircleImageRes

/**
 * 加载图片（不会使用默认占位图）
 */
fun ImageView.load0(url: String?) {
    engine.load(this, url)
}

/**
 * 使用占位图设置加载图片
 */
@JvmOverloads
fun ImageView.load(url: String?, @DrawableRes placeHolder: Int = defaultImageResAIL) {
    engine.load(this, url, placeHolder)
}

fun ImageView.load(url: String?, placeHolder: Drawable?) {
    engine.load(this, url, placeHolder)
}

fun ImageView.load(
    url: String?,
    @DrawableRes placeHolder: Int,
    @DrawableRes errorPlaceHolder: Int
) {
    engine.load(this, url, placeHolder, errorPlaceHolder)
}

fun ImageView.load(
    url: String?,
    placeHolder: Drawable?,
    errorPlaceHolder: Drawable?
) {
    engine.load(this, url, placeHolder, errorPlaceHolder)
}

fun ImageView.load(
    url: String?,
    @DrawableRes placeHolder: Int,
    errorPlaceHolder: Drawable?
) {
    engine.load(this, url, placeHolder, errorPlaceHolder)
}

fun ImageView.load(
    url: String?,
    placeHolder: Drawable?,
    @DrawableRes errorPlaceHolder: Int
) {
    engine.load(this, url, placeHolder, errorPlaceHolder)
}

/**
 * 加载圆角图片（不会使用默认占位图）
 * @param roundingRadius 圆角半径，单位px
 */
fun ImageView.loadRounded0(
    url: String?,
    roundingRadius: Int
) {
    engine.loadRounded(this, url, roundingRadius)
}

/**
 * 加载圆角图片（会使用默认圆角图片作为占位图）
 * @param roundingRadius 圆角半径，单位px
 */
@JvmOverloads
fun ImageView.loadRounded(
    url: String?,
    roundingRadius: Int = defaultImageCornerSizeAIL,
    @DrawableRes placeHolder: Int = defaultRoundedImageResAIL
) {
    engine.loadRounded(this, url, roundingRadius, placeHolder)
}

fun ImageView.loadRounded(
    url: String?,
    roundingRadius: Int,
    placeHolder: Drawable?
) {
    engine.loadRounded(this, url, roundingRadius, placeHolder)
}

fun ImageView.loadRounded(
    url: String?,
    roundingRadius: Int,
    placeHolder: Drawable?,
    error: Drawable?
) {
    engine.loadRounded(this, url, roundingRadius, placeHolder, error)
}


/**
 * 加载圆角图片,并对占位图应用圆角
 */
@Deprecated("不建议在占位图上使用转换效果，最好使用圆角效果的占位图")
fun ImageView.loadRoundedStrict(
    url: String?,
    roundingRadius: Int,
    @DrawableRes placeHolder: Int
) {
    engine.loadRounded(this, url, roundingRadius, placeHolder, true)
}

/**
 * 加载圆角图片,并对占位图应用圆角
 */
@Deprecated("不建议在占位图上使用转换效果，最好使用圆角效果的占位图")
fun ImageView.loadRoundedStrict(
    url: String?,
    roundingRadius: Int,
    placeHolder: Drawable?
) {
    engine.loadRounded(this, url, roundingRadius, placeHolder, true)
}

fun ImageView.loadCircle0(
    url: String?
) {
    engine.loadCircle(this, url)
}

@JvmOverloads
fun ImageView.loadCircle(
    url: String?,
    @DrawableRes placeHolder: Int = defaultCircleImageResAIL
) {
    engine.loadCircle(this, url, placeHolder)
}

fun ImageView.loadCircle(
    url: String?,
    placeHolder: Drawable?
) {
    engine.loadCircle(this, url, placeHolder)
}

/**
 * 加载圆形图片,并对占位图应用圆形转换
 */
@Deprecated("不建议在占位图上使用转换效果，最好使用圆角效果的占位图")
fun ImageView.loadCircleStrict(
    url: String?,
    @DrawableRes placeHolder: Int
) {
    engine.loadCircle(this, url, placeHolder, true)
}

/**
 * 加载圆形图片,并对占位图应用圆形转换
 */
@Deprecated("不建议在占位图上使用转换效果，最好使用圆角效果的占位图")
fun ImageView.loadCircleStrict(
    url: String?,
    placeHolder: Drawable?
) {
    engine.loadCircle(this, url, placeHolder, true)
}


internal fun logi(msg: String) {
    Log.i(TAG, msg)
}

internal fun logw(msg: String) {
    Log.w(TAG, msg)
}

internal fun loge(msg: String, e: Throwable? = null) {
    Log.e(TAG, msg, e)
}
