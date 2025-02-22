
package unics.okdroidarch.app

import unics.okdroid.tools.content.ProgressFakerTaskCallback
import unics.okdroid.tools.content.invokeTaskWithProgressFaker
import unics.okmultistate.StateLayout
import unics.okmultistate.uistate.LoaderUiState
import unics.okmultistate.uistate.LoadingUiState
/**
 * [ViewModelArch] progressFaker(伪百分比进度支持)
 */

suspend fun <T> ViewModelArch.requestApiWithProgressFaker(
    timeMax: Long = 6 * 1000,
    progressCallback: ProgressFakerTaskCallback,
    task: suspend () -> T
): T {
    return invokeTaskWithProgressFaker(
        timeMax,
        progressCallback = progressCallback,
        task = task
    )
}

suspend fun <T> ViewModelArch.requestApiWithProgressFaker(
    timeMax: Long = 10 * 1000,
    task: suspend () -> T
): T {
    return invokeTaskWithProgressFaker(
        timeMax = timeMax,
        progressCallback = MultiStateEventProcessFakerCallback(this, timeMax),
        task = task
    )
}

class MultiStateEventProcessFakerCallback(
    private val viewModel: ViewModelArch,
    private val timeMax: Long,
    private val waitLongMsg: String = "拼命加载中...",
    private val waitMsg: String = "正在加载，请稍后...",
) : ProgressFakerTaskCallback {
    override suspend fun onProgress(duration: Int, progress: Int) {
        if (duration > timeMax / 2) {
            viewModel.sendMultiStateMessage(LoaderUiState.loading("${waitLongMsg}$progress%"))
        } else {
            viewModel.sendMultiStateMessage(LoaderUiState.loading("${waitMsg}$progress%"))
        }
    }
}

class MultiStateLayoutProcessFakerCallback(
    private val layout: StateLayout,
    private val timeMax: Long,
    private val waitLongMsg: String = "拼命加载中...",
    private val waitMsg: String = "正在加载，请稍后...",
) : ProgressFakerTaskCallback {
    override suspend fun onProgress(duration: Int, progress: Int) {
        if (duration > timeMax / 2) {
            layout.showLoading(LoadingUiState.create("${waitLongMsg}$progress%"))
        } else {
            layout.showLoading(LoadingUiState.create("${waitMsg}$progress%"))
        }

    }
}