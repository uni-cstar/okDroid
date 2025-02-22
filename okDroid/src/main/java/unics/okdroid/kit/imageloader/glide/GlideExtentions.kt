/**
 * Created by Lucio on 2021/11/4.
 */
package unics.okdroid.kit.imageloader.glide

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.Target
import unics.okdroid.kit.imageloader.configs
import unics.okdroid.kit.imageloader.debuggable
import unics.okdroid.kit.imageloader.logi

//
///**
// * 默认请求参数
// */
//var defaultRequestOptions = RequestOptionsBas()
//    set(value) {
//        field = value
//        defaultCircleRequestOptions = value.clone().also {
//            it.transform(CenterCrop(), CircleCrop())
//        }
//    }
//
///**
// * 默认圆形请求参数
// */
//var defaultCircleRequestOptions = defaultRequestOptions.clone().also {
//    it.transform(CenterCrop(), CircleCrop())
//}

/**
 * 创建请求参数
 */
fun RequestOptionsAIL(): RequestOptions {
    if (debuggable) {
        logi("create RequestOptionsAIL")
    }
    return RequestOptions()
        .skipMemoryCache(configs.isMemoryCacheEnabled)
        .diskCacheStrategy(if (!configs.isDiskCacheEnabled) DiskCacheStrategy.NONE else DiskCacheStrategy.AUTOMATIC)
        .format(
            DecodeFormat.PREFER_RGB_565
        )
//        .onlyRetrieveFromCache(true)
}

/**
 * 加载原始图片：图片不适宜过大，否则容易OOM
 */
fun ImageView.loadOriginalBitmap(url: String?) {
    Glide.with(this)
        .asBitmap()
        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        .load(url)
        .into(BitmapImageViewTarget(this))
}

/**
 * 加载原始图片：图片不适宜过大，否则容易OOM
 */
fun ImageView.loadOriginalBitmap(url: String?, ph: Int) {
    Glide.with(this)
        .asBitmap()
        .placeholder(ph)
        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        .load(url)
        .into(BitmapImageViewTarget(this))
}
