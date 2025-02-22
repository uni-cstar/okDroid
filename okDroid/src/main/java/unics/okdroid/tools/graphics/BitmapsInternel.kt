@file:JvmName("BitmapsKt")
@file:JvmMultifileClass

package unics.okdroid.tools.graphics

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * 通过decode读取图片Bounds信息创建[BitmapFactory.Options]:通常用于获取图片宽高等信息
 */
fun newBitmapOptionsByDecodeBounds(path: String): BitmapFactory.Options {
    val optionsInfo = BitmapFactory.Options()
    // 这里设置true的时候，decode时候不会返回Bitmap，只会将图片宽高等信息放入Options中。
    optionsInfo.inJustDecodeBounds = true
    decodeBitmap(path, optionsInfo)
    return optionsInfo
}

fun newBitmapOptionsByDecodeBounds(data: ByteArray): BitmapFactory.Options {
    val optionsInfo = BitmapFactory.Options()
    // 这里设置true的时候，decode时候不会返回Bitmap，只会将图片宽高等信息放入Options中。
    optionsInfo.inJustDecodeBounds = true
    decodeBitmap(data, optionsInfo)
    return optionsInfo
}

fun newBitmapOptionsByDecodeBounds(ctx: Context, uri: Uri): BitmapFactory.Options {
    val optionsInfo = BitmapFactory.Options()
    // 这里设置true的时候，decode时候不会返回Bitmap，只会将图片宽高等信息放入Options中。
    optionsInfo.inJustDecodeBounds = true
    decodeBitmap(ctx, uri, optionsInfo)
    return optionsInfo
}

/**
 * 创建一个合适的用于decode图片的[BitmapFactory.Options]
 * @param path
 * @param maxWidth 最大宽度
 * @param maxHeight 最大高度
 * @return
 */
fun newBitmapOptionsForDecode(
    path: String,
    maxWidth: Int,
    maxHeight: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): BitmapFactory.Options {
    val options = newBitmapOptionsByDecodeBounds(path)
    options.inSampleSize =
        calculateInSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight)
    options.inJustDecodeBounds = false
//    if (Build.VERSION.SDK_INT < 21) {
//        options.inPurgeable = true
//        options.inInputShareable = true
//    }
    options.inPreferredConfig = config
    return options
}

/**
 * 获取图片的BitmapFactory.Options
 */
fun newBitmapOptionsForDecode(
    data: ByteArray,
    maxWidth: Int,
    maxHeight: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): BitmapFactory.Options {
    val options = newBitmapOptionsByDecodeBounds(data)
    options.inSampleSize =
        calculateInSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight)
    options.inJustDecodeBounds = false
    options.inPreferredConfig = config
    return options
}

/**
 * 获取图片的BitmapFactory.Options
 */
fun newBitmapOptionsForDecode(
    ctx: Context,
    uri: Uri,
    maxWidth: Int,
    maxHeight: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): BitmapFactory.Options {
    val options = newBitmapOptionsByDecodeBounds(ctx, uri)
    options.inSampleSize =
        calculateInSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight)
    options.inJustDecodeBounds = false
    options.inPreferredConfig = config
    return options
}

/**
 * 通过需求的宽和高简单计算适当的InSampleSize
 *
 * @param width 图片宽
 * @param height 图片高
 * @param maxWidth  需求宽
 * @param maxHeight 需求高
 * @return
 */
fun calculateInSampleSize(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Int {
    require(maxWidth > 0) {
        "maxWidth param is zero."
    }
    require(maxHeight > 0) {
        "maxHeight param is zero."
    }
    val inSampleSize = max(width * 1.0 / maxWidth, height * 1.0 / maxHeight).toInt()
    return max(1, inSampleSize)
}


/**
 * 获取缩放之后的图片
 * @param maxWidth 目标宽度
 * @param maxHeight 目标高度
 * @param imagePath 图片路径
 * @param config 压缩配置，默认[Bitmap.Config.ARGB_8888]
 */
fun decodeBitmap(
    imagePath: String,
    maxWidth: Int,
    maxHeight: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap? {
    val bmOptions = newBitmapOptionsForDecode(imagePath, maxWidth, maxHeight, config)
    return decodeBitmap(imagePath, bmOptions)
}


fun decodeBitmap(
    data: ByteArray,
    maxWidth: Int,
    maxHeight: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap? {
    val bmOptions = newBitmapOptionsForDecode(data, maxWidth, maxHeight, config)
    return decodeBitmap(data, bmOptions)
}

/**
 * 解析Bitmap的公用方法
 */
fun decodeBitmap(ctx: Context, uri: Uri, options: BitmapFactory.Options): Bitmap? {
    return ctx.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    }
}

fun decodeBitmap(
    ctx: Context, uri: Uri,
    maxWidth: Int,
    maxHeight: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap? {
    val bmOptions = newBitmapOptionsForDecode(ctx, uri, maxWidth, maxHeight, config)
    return decodeBitmap(ctx, uri, bmOptions)
}


/**
 * 解析Bitmap的公用方法.
 */
fun decodeBitmap(path: String, options: BitmapFactory.Options): Bitmap? {
    return BitmapFactory.decodeFile(path, options)
}

/**
 * 解析Bitmap
 * @param base64 图片base64字符串数据
 */
fun decodeBitmap(base64: String, base64Type: Int = Base64.NO_WRAP): Bitmap? {
    var content = base64
    val dataTag = "data:image/jpeg;base64,"
    if (content.startsWith(dataTag)) {
        content = base64.substring(dataTag.length)
    }
    val byteArray = Base64.decode(content, base64Type)
    return decodeBitmap(byteArray)
}

fun decodeBitmapInBase64Compat(
    base64: String,
    maxWidth: Int,
    maxHeight: Int,
    base64Type: Int = Base64.NO_WRAP
): Bitmap? {
    var content = base64
    val dataTag = "data:image/jpeg;base64,"
    if (content.startsWith(dataTag)) {
        content = base64.substring(dataTag.length)
    }
    val byteArray = Base64.decode(content, base64Type)
    val options = newBitmapOptionsForDecode(byteArray, maxWidth, maxHeight, Bitmap.Config.ARGB_8888)
    return decodeBitmap(byteArray, options = options) ?: decodeBitmapInYuv(
        byteArray,
        options.also {
            if (it.outWidth <= 0) {
                it.outWidth = maxWidth
            }
            if (it.outHeight <= 0) {
                it.outHeight = maxHeight
            }
        }
    )
}

@Deprecated(message = "该方法还未测试过，来源于网络：https://blog.csdn.net/jian11058/article/details/84976021")
private fun decodeBitmapInYuv(
    data: ByteArray,
    options: BitmapFactory.Options, quality: Int = 80
): Bitmap? {
    try {
        val yuvImage = YuvImage(
            data,
            ImageFormat.NV21,
            options.outWidth,
            options.outHeight,
            null
        );//20、20分别是图的宽度与高度

        var baos: ByteArrayOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                Rect(0, 0, options.outWidth, options.outHeight),
                quality,
                baos
            );//80--JPG图片的质量[0-100],100最高
            val compressedBytes = baos.toByteArray()
            return decodeBitmap(compressedBytes, 0, compressedBytes.size, options = options)
        } finally {
            baos?.close()
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        return null
    }
}

/**
 * 解析Bitmap的公用方法
 */
fun decodeBitmap(data: ByteArray, options: BitmapFactory.Options): Bitmap? {
    return BitmapFactory.decodeByteArray(data, 0, data.size, options)
}

/**
 * 解析Bitmap的公用方法
 */
@JvmOverloads
fun decodeBitmap(
    data: ByteArray,
    offset: Int = 0,
    length: Int = data.size,
    options: BitmapFactory.Options? = null,
): Bitmap? {
    return BitmapFactory.decodeByteArray(data, offset, length, options)
}