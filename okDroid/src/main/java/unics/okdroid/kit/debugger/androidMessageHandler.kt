package unics.okdroid.kit.debugger

import android.content.Context
import android.widget.Toast
import java.io.File

/**
 * Create by luochao
 * on 2023/12/19
 */

fun toastMessageHandler(context: Context, length: Int = Toast.LENGTH_SHORT): MessageHandler {
    return object : MessageHandler {
        val ctx = context.applicationContext
        override fun handle(tag: String, message: String) {
            Toast.makeText(ctx, "$tag:$message", length).show()
        }
    }
}

fun fileMessageHandler(context: Context): MessageHandler {
    return object : MessageHandler {
        val ctx = context.applicationContext
        override fun handle(tag: String, message: String) {
            File(ctx.externalCacheDir, "fileMessageHandler").apply {
                if (exists().not()) {
                    createNewFile()
                }
                appendText("$tag:$message")
            }
        }
    }
}
