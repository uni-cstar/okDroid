package unics.okdroid.tools.ui

import androidx.annotation.StringRes
import unics.okdroid.globalContext

class ToastException(message: String, val length: Int = ToastUI.LENGTH_SHORT) :
    RuntimeException(message) {
    constructor(
        @StringRes textId: Int,
        length: Int = ToastUI.LENGTH_SHORT
    ) : this(globalContext.getString(textId), length)
}