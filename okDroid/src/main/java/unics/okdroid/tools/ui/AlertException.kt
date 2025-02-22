package unics.okdroid.tools.ui

import androidx.annotation.StringRes
import unics.okdroid.globalContext

/**
 * @param cancelable 是否可以取消
 */
class AlertException(message: String, val cancelable: Boolean = true) : RuntimeException(message) {
    constructor(@StringRes textId: Int, cancelable: Boolean = true) : this(
        globalContext.getString(textId),
        cancelable
    )
}