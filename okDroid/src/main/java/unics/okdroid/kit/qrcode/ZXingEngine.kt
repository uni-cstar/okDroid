package unics.okdroid.kit.qrcode

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.Hashtable

/**
 * Zxing引擎
 */
internal object ZXingEngine : QRCodeEngine {
    private const val BLACK: Int = -0x1000000
    private const val WHITE = -0x1
    override fun generate(content: String, size: Int, margin: Int): Bitmap? {
        if (content.isEmpty()) {
            return null
        }
        return try {
            val hints: Hashtable<EncodeHintType, Any?> = Hashtable<EncodeHintType, Any?>()
            hints[EncodeHintType.CHARACTER_SET] = "utf-8"
            hints[EncodeHintType.MARGIN] = margin // 设置二维码边距
            //如果LEVEL是Q则四周默认有白边，而如果是H则没有
            //如果设置Q又不想要白边可通过后面的deleteWhite先删除白边（删除后比期望的size小了，如果严格要求大小可以再scaleBitmap）
            hints[EncodeHintType.ERROR_CORRECTION] =
                if (margin <= 0) ErrorCorrectionLevel.H else ErrorCorrectionLevel.Q
            val rawMatrix = MultiFormatWriter().encode(
                content, BarcodeFormat.QR_CODE, size, size, hints
            ) ?: return null
            val width = rawMatrix.width
            val height = rawMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (rawMatrix[x, y]) {
                        pixels[y * width + x] = BLACK
                    } else {
                        pixels[y * width + x] = WHITE
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * 删除白边
     * 参考：https://blog.csdn.net/Deaht_Huimie/article/details/79974581 ，使用的最后一种方法，通过改变Matrix实现
     */
    private fun deleteWhite(matrix: BitMatrix): BitMatrix {
        val rec = matrix.enclosingRectangle
        val resWidth = rec[2]
        val resHeight = rec[3]
        val resMatrix = BitMatrix(resWidth, resHeight)
        resMatrix.clear()
        for (i in 0 until resWidth) {
            for (j in 0 until resHeight) {
                if (matrix[i + rec[0], j + rec[1]]) {
                    resMatrix[i] = j
                }
            }
        }
        return resMatrix
    }

    private fun scaleBitmap(origin: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        return try {
            val aspectRatio = origin.width.toFloat() / origin.height.toFloat()
            val (scaledWidth, scaledHeight) = when {
                newWidth / aspectRatio > newHeight ->
                    Pair((newHeight * aspectRatio).toInt(), newHeight)

                else ->
                    Pair(newWidth, (newWidth / aspectRatio).toInt())
            }

            Bitmap.createScaledBitmap(origin, scaledWidth, scaledHeight, true)
                .also { scaledBitmap ->
                    if (origin != scaledBitmap && !origin.isRecycled) {
                        origin.recycle()  // 安全回收原始位图
                    }
                }
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: OutOfMemoryError) {
            System.gc()
            null
        }
    }
}