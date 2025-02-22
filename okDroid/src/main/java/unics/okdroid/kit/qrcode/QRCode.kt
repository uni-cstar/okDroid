package unics.okdroid.kit.qrcode

import android.graphics.Bitmap


object QRCode {

    //默认zxing引擎，使用的时候确保已经引入了zxing的依赖
    @JvmStatic
    private var engine: QRCodeEngine = ZXingEngine

    @JvmStatic
    fun setEngine(engine: QRCodeEngine) {
        QRCode.engine = engine
    }

    /**
     * 生成二维码
     */
    @JvmOverloads
    @JvmStatic
    fun generate(content: String, size: Int, margin: Int = 0): Bitmap? {
        return engine.generate(content, size, margin)
    }

}

