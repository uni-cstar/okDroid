package unics.okdroid.kit.qrcode

import android.graphics.Bitmap

/**
 * 二维码引擎
 */
interface QRCodeEngine {

    /**
     * 生成二维码图片
     */
    fun generate(content: String, size: Int): Bitmap? = generate(content, size, 0)
    fun generate(content: String, size: Int, margin: Int): Bitmap?

}