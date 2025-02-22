@file:JvmName("TextsKt")
@file:JvmMultifileClass

package unics.okdroid.widget.text

import android.text.InputFilter
import android.widget.TextView

internal inline fun TextView.appendInputFilter(filter: InputFilter) {
    val filters = this.filters
    if (filters == null || filters.isEmpty()) {
        this.filters = arrayOf(filter)
    } else {
        this.filters = filters + filter
    }
}

