package unics.example.okdroidarch

import unics.okcore.udf.UDFState

/**
 * @author: chaoluo10
 * @date: 2024/6/21
 * @desc:
 */
data class SampleUiState(
    val text: String,
    val count: Int,
    val time: Long = System.currentTimeMillis()
) : UDFState {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SampleUiState

        if (text != other.text) return false
        if (count != other.count) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + count
        return result
    }

    override fun toString(): String {
        return " ${this.hashCode()}@ text:$text count:$count time:$time"
    }
}